package com.houndjo.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapping the {@code quran_verse} table. Immutable, global reference data — no
 * {@code organization_id}, no audit columns. Positions a verse within the 604-page Medina
 * Mushaf layout (page/juz/hizb/quarter).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "quran_verse", uniqueConstraints = @UniqueConstraint(columnNames = {"surah_number", "verse_number"}))
public class QuranVerseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "surah_number", nullable = false)
    private Short surahNumber;

    @Column(name = "verse_number", nullable = false)
    private Short verseNumber;

    @Column(name = "page", nullable = false)
    private Short page;

    @Column(name = "juz", nullable = false)
    private Short juz;

    @Column(name = "hizb", nullable = false)
    private Short hizb;

    @Column(name = "hizb_quarter", nullable = false)
    private Short hizbQuarter;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuranVerseEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
