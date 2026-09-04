package com.houndjo.infrastructure.adapter.out.persistence.mapper;

import com.houndjo.domain.models.quran.Surah;
import com.houndjo.domain.models.quran.Verse;
import com.houndjo.infrastructure.adapter.out.persistence.entity.QuranSurahEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.QuranVerseEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between the Quran reference JPA entities and their domain counterparts.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface QuranReferenceMapper {

    Surah toDomain(QuranSurahEntity entity);

    Verse toDomain(QuranVerseEntity entity);

    List<Verse> toDomainVerses(List<QuranVerseEntity> entities);
}
