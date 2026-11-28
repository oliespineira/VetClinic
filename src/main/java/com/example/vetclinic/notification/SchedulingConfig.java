package com.example.vetclinic.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/** Turns on @Scheduled jobs (the daily reminder run) and provides a Clock that tests can replace. */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
