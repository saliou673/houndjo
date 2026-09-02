package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.SecuritySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecuritySettingsRepository extends JpaRepository<SecuritySettingsEntity, Long> {}
