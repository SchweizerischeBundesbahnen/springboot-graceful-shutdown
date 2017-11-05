/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;


/**
 * This HealthCheck register itself by Springboot Autoconfiguration
 * @see GracefulShutdownAutoConfiguration if Spring-Boot-Actuator package is registered
 * After the JVM wants to shutdown, this healtcheck will return first false.
 * So http://yourhost:8080/health will return false from then on.
 * Then the Springcontext will be shutdown with the configured delay for example 30 seconds.
 * The healthcheck will make sure, that Openshift knows the application is down for
 * removing it from the service.
 */
public class GracefulShutdownHealthCheck implements HealthIndicator, IProbeController {
    private static final Log log = LogFactory.getLog(GracefulShutdownHealthCheck.class);

    public static final String GRACEFULSHUTDOWN = "Gracefulshutdown";
    private Health health;

    GracefulShutdownHealthCheck() {
        setReady(true);
    }

    public Health health() {
        return health;
    }

    public void setReady(boolean ready) {
        if (ready) {
            health = new Health.Builder().withDetail(GRACEFULSHUTDOWN, "application up").up().build();
            log.info("Gracefulshutdown healthcheck up");
        } else {
            health = new Health.Builder().withDetail(GRACEFULSHUTDOWN, "gracefully shutting down").down().build();
            log.info("Gracefulshutdown healthcheck down");
        }
    }
}


