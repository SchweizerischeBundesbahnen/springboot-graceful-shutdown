/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Map;


/**
 * The Shutdownhook will gracefully shutdown the app.
 * When a SIGTERM Signal comes to the JVM, the {@link GracefulShutdownHook#run()} will be executed.
 * 1. The Actuator Healthcheck will be down if actuator is used otherwise your implemented ProbeController
 * Rest Service /ready is set to false so it will return 404
 * 2. Thread.wait with the configured wait time will cause the shutdownprocess to wait.
 * This is necessary to give Openshift time to execute the readyness Probe. Once the readynessprobe is false,
 * Openshift Removes the Pod for future Requests
 * 3. All Open Requests will be finished.
 * 4. After waiting the configured estaGracefulShutdownWaitSeconds, the applicationContext will be closed.
 * <p>
 * For the ShutdownHook to work, you need to
 * provide a ProbeController for the readyness and liveness probe which must implement
 * the interface {@link IProbeController}
 *
 * @author ue64007
 */
class GracefulShutdownHook implements Runnable {
    protected static final String GRACEFUL_SHUTDOWN_WAIT_SECONDS = "estaGracefulShutdownWaitSeconds";
    private static final String DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SECONDS = "20";

    private static final Log log = LogFactory.getLog(GracefulShutdownHook.class);

    private final ConfigurableApplicationContext applicationContext;

    GracefulShutdownHook(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Will first set the readyness or healthchecks to false. You can implement your own RestController which implements the {@link IProbeController}
     * After the Application will wait for the configured timeout till it finally shutdown the spring context.
     */
    public void run() {
        setReadynessToFalse();
        delayShutdownSpringContext();
        shutdownSpringContext();
    }

    private void shutdownSpringContext() {
        log.info("Spring Application context starting to shutdown");
        applicationContext.close();
        log.info("Spring Application context is shutdown");
    }

    private void setReadynessToFalse() {
        log.info("Setting readyness for application to false, so the application doesn't receive new connections from Openshift");
        final Map<String, IProbeController> probeControllers = applicationContext.getBeansOfType(IProbeController.class);
        if (probeControllers.size() < 1) {
            log.error("Could not find a ProbeController Bean. Your ProbeController needs to implement the Interface: " + IProbeController.class.getName());
        }
        if (probeControllers.size() > 1) {
            log.warn("You have more than one ProbeController for Readyness-Check registered. " +
                    "Most probably one as Rest service and one in automatically configured as Actuator health check.");
        }
        for (IProbeController probeController : probeControllers.values()) {
            probeController.setReady(false);
        }
    }

    /**
     * Delaying Springcontext shutdown, because open REST calls should be finished
     * and as long the Openshift HA Proxy send still calls to the App, we need
     * to give Openshift time to discover the readynessprobe is false.
     * After the readynessprobe is false, we can
     */
    private void delayShutdownSpringContext() {
        try {
            int shutdownWaitSeconds = getShutdownWaitSeconds();
            log.info("Gonna wait for " + shutdownWaitSeconds + " seconds before shutdown SpringContext!");
            Thread.sleep(shutdownWaitSeconds * 1000);
        } catch (InterruptedException e) {
            log.error("Error while gracefulshutdown Thread.sleep", e);
        }
    }

    /**
     * Tries to get the value from the Systemproperty estaGracefulShutdownWaitSeconds,
     * otherwise it tries to read it from the application.yml, if there also not found 20 is returned
     *
     * @return The configured seconds, default 20
     */
    private int getShutdownWaitSeconds() {
        String waitSeconds = System.getProperty(GRACEFUL_SHUTDOWN_WAIT_SECONDS);
        if (StringUtils.isEmpty(waitSeconds)) {
            waitSeconds = applicationContext.getEnvironment().getProperty(GRACEFUL_SHUTDOWN_WAIT_SECONDS, DEFAULT_GRACEFUL_SHUTDOWN_WAIT_SECONDS);
        }
        return Integer.parseInt(waitSeconds);
    }
}