/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StopWatch;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class GracefulShutdownHookTest {
    private ConfigurableApplicationContext appContext;
    private ConfigurableEnvironment confEnv;
    private GracefulShutdownHealthCheck healthCheck;
    private static final int SHUTDOWN_WAIT_S = 3;


    @Before
    public void setup() {
        // Prepare
        appContext = Mockito.mock(ConfigurableApplicationContext.class);
        confEnv = Mockito.mock(ConfigurableEnvironment.class);
        healthCheck = new GracefulShutdownHealthCheck();
        final HashMap<String, IProbeController> beans = new HashMap<>();
        beans.put(GracefulShutdownHealthCheck.class.getName(), healthCheck);

        // Test
        when(confEnv.getProperty(GracefulShutdownHook.GRACEFUL_SHUTDOWN_WAIT_SECONDS, "20")).thenReturn(String.valueOf(SHUTDOWN_WAIT_S));
        when(appContext.getEnvironment()).thenReturn(confEnv);
        when(appContext.getBeansOfType(IProbeController.class)).thenReturn(beans);
    }

    @Test
    public void testBeforeShutdown() {
        GracefulShutdownHook testee = new GracefulShutdownHook(appContext);
        healthCheck.setReady(true);

        assertEquals(Status.UP, healthCheck.health().getStatus());
    }

    @Test
    public void testShutdown() throws Exception {
        // Prepare
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        GracefulShutdownHook testee = new GracefulShutdownHook(appContext);
        healthCheck.setReady(true);

        // Modify
        testee.run();

        // Test
        asyncSpringContextShutdownDelayedAssert();
        assertEquals(Status.DOWN, healthCheck.health().getStatus());
        verify(appContext, times(1)).close();
        stopWatch.stop();
        assertTrue(stopWatch.getTotalTimeSeconds() >= SHUTDOWN_WAIT_S);
    }

    /**
     * Asserts if the Readynessprobe is down
     */
    private void asyncSpringContextShutdownDelayedAssert() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep((SHUTDOWN_WAIT_S-1)*1000);
                    assertEquals(Status.DOWN, healthCheck.health().getStatus());
                    verify(appContext, never()).close();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }
}