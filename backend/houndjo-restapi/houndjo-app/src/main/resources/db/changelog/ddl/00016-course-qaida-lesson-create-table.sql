--liquibase formatted sql
--changeset houndjo:00016-course-qaida-lesson-create-table
CREATE TABLE IF NOT EXISTS course_qaida_lesson (
    course_id       BIGINT       NOT NULL,
    display_order   INTEGER      NOT NULL,
    lesson_name     VARCHAR(150) NOT NULL,
    CONSTRAINT pk_course_qaida_lesson PRIMARY KEY (course_id, display_order),
    CONSTRAINT fk_course_qaida_lesson_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);
