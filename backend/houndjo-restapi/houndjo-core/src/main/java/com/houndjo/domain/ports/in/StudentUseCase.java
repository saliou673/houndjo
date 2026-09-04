package com.houndjo.domain.ports.in;

import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.student.Student;
import com.houndjo.domain.models.student.StudentFilter;
import java.time.LocalDate;

/**
 * Use case for managing students (pedagogical profiles) within the active organization.
 */
public interface StudentUseCase {

    /**
     * Returns the students of the active organization matching the filter, paginated.
     *
     * @param filter search criteria
     * @param page   zero-based page index
     * @param size   maximum items per page
     * @return paginated students
     */
    PagedResult<Student> findAll(StudentFilter filter, int page, int size);

    /**
     * Returns a student by its identifier within the active organization.
     *
     * @param id the student identifier
     * @return the matching student
     */
    Student getById(Long id);

    /**
     * Creates a new student in the active organization.
     *
     * @param firstName     given name
     * @param lastName      family name
     * @param birthDate     optional date of birth
     * @param gender        optional gender
     * @param guardianName  optional guardian name
     * @param guardianPhone optional guardian phone number
     * @return the created student
     */
    Student create(
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone);

    /**
     * Updates an existing student of the active organization.
     *
     * @param id            the student identifier
     * @param firstName     new given name
     * @param lastName      new family name
     * @param birthDate     new date of birth
     * @param gender        new gender
     * @param guardianName  new guardian name
     * @param guardianPhone new guardian phone number
     * @return the updated student
     */
    Student update(
            Long id,
            String firstName,
            String lastName,
            LocalDate birthDate,
            UserGender gender,
            String guardianName,
            String guardianPhone);

    /**
     * Deletes a student of the active organization.
     *
     * @param id the student identifier
     */
    void delete(Long id);
}
