package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.RevelationPlace;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapping the {@code quran_surah} table. Immutable, global reference data — no
 * {@code organization_id}, no audit columns.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "quran_surah")
public class QuranSurahEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "number")
    private Short number;

    @Column(name = "name_arabic", nullable = false, length = 80)
    private String nameArabic;

    @Column(name = "name_transliteration", nullable = false, length = 80)
    private String nameTransliteration;

    @Column(name = "name_fr", nullable = false, length = 80)
    private String nameFr;

    @Column(name = "name_en", nullable = false, length = 80)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "revelation_place", nullable = false, length = 10)
    private RevelationPlace revelationPlace;

    @Column(name = "verse_count", nullable = false)
    private Short verseCount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuranSurahEntity other)) return false;
        return number != null && number.equals(other.number);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
