package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.domain.enumerations.AppConfigurationCategory;
import com.houndjo.infrastructure.adapter.out.persistence.entity.AppConfigurationEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link AppConfigurationEntity}.
 */
@Repository
public interface AppConfigurationRepository
        extends JpaRepository<AppConfigurationEntity, Long>, JpaSpecificationExecutor<AppConfigurationEntity> {

    Optional<AppConfigurationEntity> findByCategoryAndCode(AppConfigurationCategory category, String code);

    boolean existsByCategoryAndCode(AppConfigurationCategory category, String code);

    boolean existsByCategoryAndCodeAndIdNot(AppConfigurationCategory category, String code, Long id);

    boolean existsByCategoryAndCodeAndActiveTrue(AppConfigurationCategory category, String code);
}
