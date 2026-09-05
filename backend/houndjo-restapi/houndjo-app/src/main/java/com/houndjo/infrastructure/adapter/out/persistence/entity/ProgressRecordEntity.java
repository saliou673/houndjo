package com.houndjo.infrastructure.adapter.out.persistence.entity;

import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
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
 * JPA entity mapping the {@code progress_record} table. Portion columns are nullable and
 * populated depending on {@code flow}; see
 * {@link com.houndjo.infrastructure.adapter.out.persistence.mapper.ProgressRecordMapper} for how
 * they fold into/out of the polymorphic {@code PortionRef} domain model.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "progress_record")
public class ProgressRecordEntity extends AuditableEntity<Long> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow", nullable = false, length = 15)
    private ProgressFlow flow;

    @Column(name = "from_surah")
    private Short fromSurah;

    @Column(name = "from_verse")
    private Short fromVerse;

    @Column(name = "to_surah")
    private Short toSurah;

    @Column(name = "to_verse")
    private Short toVerse;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(name = "chapter_no")
    private Short chapterNo;

    @Column(name = "page_no")
    private Short pageNo;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fluency", length = 15)
    private FluencyRating fluency;

    @Enumerated(EnumType.STRING)
    @Column(name = "tajweed", length = 15)
    private FluencyRating tajweed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ProgressStatus status;

    @Column(name = "note")
    private String note;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProgressRecordEntity other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
