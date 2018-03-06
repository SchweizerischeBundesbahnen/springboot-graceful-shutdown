/*
 * Copyright (C) Schweizerische Bundesbahnen SBB, 2017.
 */

package ch.sbb.esta.openshift.gracefullshutdown;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This configuration will be conditional executed if the spring-boot-actuator
 * package is present.
 */
@Configuration
@ConditionalOnClass(HealthIndicator.class)
public class GracefulShutdownAutoConfiguration {
    @Bean
    HealthIndicator gracefulShutdownHealthCheck() {
        return new GracefulShutdownHealthCheck();
    }
}
