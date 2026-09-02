package com.houndjo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Required to enable scheduling.
 * <p>It allows using {@code @Scheduled} annotations.
 */
@Configuration
@EnableScheduling
public class SchedulerConfiguration {}
