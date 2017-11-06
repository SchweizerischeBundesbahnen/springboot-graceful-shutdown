/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;


import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Custom ESTA Spring Application Launcher which uses the {@link GracefulShutdownHook}
 * to gracefully shutdown the SpringBootApp. For the ShutdownHook to work, you need to
 * provide a ProbeController for the readyness and liveness probe which must implement
 * the interface {@link IProbeController}
 */
public class GracefulshutdownSpringApplication {
    public static void run(Class<?> appClazz, String... args) {
        SpringApplication app = new SpringApplication(appClazz);
        app.setRegisterShutdownHook(false);
        ConfigurableApplicationContext applicationContext = app.run(args);
        Runtime.getRuntime().addShutdownHook(new Thread(new GracefulShutdownHook(applicationContext)));
    }
}
