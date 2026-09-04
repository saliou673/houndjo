package com.houndjo.infrastructure.adapter.out.persistence.repository;

import com.houndjo.infrastructure.adapter.out.persistence.entity.QuranSurahEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data JPA repository for {@link QuranSurahEntity}.
 */
@Transactional(readOnly = true)
public interface QuranSurahRepository extends JpaRepository<QuranSurahEntity, Short> {}
