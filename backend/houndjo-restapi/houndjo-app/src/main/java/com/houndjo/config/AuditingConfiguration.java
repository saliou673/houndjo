package com.houndjo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Required to make JPA auditing work (eg: @CreatedBy, @LastModifiedBy annotations).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditingConfiguration {}
