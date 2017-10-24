/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.actuate.health.Status;

public class GracefulShutdownHealthCheckTest {
    @Test
    public void healthUp() throws Exception {
        final GracefulShutdownHealthCheck testee = new GracefulShutdownHealthCheck();

        testee.setReady(true);

        Assert.assertEquals(Status.UP, testee.health().getStatus());
    }

    @Test
    public void healthDown() throws Exception {
        final GracefulShutdownHealthCheck testee = new GracefulShutdownHealthCheck();

        testee.setReady(false);

        Assert.assertEquals(Status.DOWN, testee.health().getStatus());
    }
}