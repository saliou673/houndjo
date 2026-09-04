--liquibase formatted sql
--changeset houndjo:00019-enrollment-course-create-table
CREATE TABLE IF NOT EXISTS enrollment_course (
    enrollment_id BIGINT NOT NULL,
    course_id     BIGINT NOT NULL,
    CONSTRAINT pk_enrollment_course PRIMARY KEY (enrollment_id, course_id),
    CONSTRAINT fk_enrollment_course_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment (id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollment_course_course FOREIGN KEY (course_id) REFERENCES course (id) ON DELETE CASCADE
);
