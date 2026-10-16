// config/SchedulingConfig.java
package com.mphasis.taskpulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on the background job that keeps the master Excel files in sync. */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}