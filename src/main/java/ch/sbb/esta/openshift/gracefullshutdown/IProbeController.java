/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;



/**
 * This Interface will be used for Gracefully shutdown the App by setting the readynessprobe to false while shutting down the app.
 * to set the Readyness Probe to false. Your REST Controller who serves the Readyness and Livenessprobe
 * needs to implement this interface. If you don't implement this interface, the actuator Healthcheck will set to false.
 *
 */
public interface IProbeController {
    /**
     * This Methodwill be called when the Springboot App gets a SIGTERM Signal
     * @param ready give false to set the readynessprobe to false
     */
    void setReady(boolean ready);
}
