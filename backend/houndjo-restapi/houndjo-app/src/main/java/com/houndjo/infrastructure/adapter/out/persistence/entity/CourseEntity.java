package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
 * JPA entity mapping the {@code course} table. Type-specific columns are nullable and populated
 * depending on {@code type}; see {@link com.houndjo.infrastructure.adapter.out.persistence.mapper.CourseMapper}
 * for how they fold into/out of the polymorphic {@code TrackingConfig} domain model.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "course")
public class CourseEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private CourseType type;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "quran_mode", length = 10)
    private QuranMode quranMode;

    @Column(name = "quran_scope_from_juz")
    private Short quranScopeFromJuz;

    @Column(name = "quran_scope_to_juz")
    private Short quranScopeToJuz;

    @Column(name = "book_title", length = 150)
    private String bookTitle;

    @Column(name = "book_total_chapters")
    private Short bookTotalChapters;

    @Column(name = "book_total_pages")
    private Short bookTotalPages;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CourseEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
