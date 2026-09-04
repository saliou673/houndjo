package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.EnrollmentStatus;
import com.houndjo.domain.exceptions.DuplicateActiveEnrollmentException;
import com.houndjo.domain.models.enrollment.Enrollment;
import com.houndjo.domain.ports.out.persistenceport.EnrollmentPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.EnrollmentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateEnrollmentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateEnrollmentCoursesRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class EnrollmentControllerTest extends IntegrationTest {

    private static final String ENROLLMENTS_API = "/api/v1/enrollments";
    private static final String CLASSES_API = "/api/v1/classes";
    private static final String STUDENTS_API = "/api/v1/students";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    @Autowired
    private EnrollmentPersistencePort enrollmentPersistencePort;

    // region enroll

    @Test
    void shouldEnrollStudentInClassAndCourses() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());

        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.id(), schoolClass.id(), Set.of(course.id()));

        EnrollmentDTO result = mockMvc(
                MockMvcRequestBuilders.post(ENROLLMENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                EnrollmentDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.studentId()).isEqualTo(student.id());
        assertThat(result.studentName()).isEqualTo(student.firstName() + " " + student.lastName());
        assertThat(result.classId()).isEqualTo(schoolClass.id());
        assertThat(result.className()).isEqualTo(schoolClass.name());
        assertThat(result.courseIds()).containsExactly(course.id());
        assertThat(result.status()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(result.endDate()).isNull();
    }

    @Test
    void shouldRejectDoubleActiveEnrollmentInSameClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        StudentDTO student = createStudent(organization.getId());
        enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        CreateEnrollmentRequest request = new CreateEnrollmentRequest(student.id(), schoolClass.id(), Set.of());

        mockMvc.perform(MockMvcRequestBuilders.post(ENROLLMENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldTranslateActiveEnrollmentConstraintViolation() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        StudentDTO student = createStudent(organization.getId());
        enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        Enrollment duplicate =
                Enrollment.create(organization.getId(), student.id(), schoolClass.id(), Set.of(), LocalDate.now());

        assertThatThrownBy(() -> enrollmentPersistencePort.save(duplicate))
                .isInstanceOf(DuplicateActiveEnrollmentException.class);
    }

    @Test
    void shouldRejectCourseOutsideClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        ClassDTO otherClass = createClass(organization.getId(), "CP2");
        CourseDTO otherClassCourse = createCourse(organization.getId(), otherClass.id());
        StudentDTO student = createStudent(organization.getId());

        CreateEnrollmentRequest request =
                new CreateEnrollmentRequest(student.id(), schoolClass.id(), Set.of(otherClassCourse.id()));

        mockMvc.perform(MockMvcRequestBuilders.post(ENROLLMENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // endregion

    // region list & get

    @Test
    void shouldListAndFilterEnrollments() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        StudentDTO student = createStudent(organization.getId());
        enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        PaginatedResult<EnrollmentDTO> result = mockMvc(
                MockMvcRequestBuilders.get(ENROLLMENTS_API + "?classId=" + schoolClass.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).studentId()).isEqualTo(student.id());
    }

    @Test
    void shouldNotGetEnrollmentFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClassAs(otherOrganization.getId(), otherOwner);
        StudentDTO otherStudent = createStudent(otherOrganization.getId(), otherOwner);
        EnrollmentDTO otherEnrollment =
                enroll(otherOrganization.getId(), otherStudent.id(), otherClass.id(), Set.of(), otherOwner);

        mockMvc.perform(MockMvcRequestBuilders.get(ENROLLMENTS_API + "/" + otherEnrollment.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "enrollment:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region courses & end

    @Test
    void shouldAddAndRemoveCourses() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        EnrollmentDTO created = enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        UpdateEnrollmentCoursesRequest addRequest = new UpdateEnrollmentCoursesRequest(Set.of(course.id()), null);
        EnrollmentDTO afterAdd = mockMvc(
                MockMvcRequestBuilders.patch(ENROLLMENTS_API + "/" + created.id() + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(addRequest)),
                EnrollmentDTO.class,
                status().isOk());
        assertThat(afterAdd.courseIds()).containsExactly(course.id());

        UpdateEnrollmentCoursesRequest removeRequest = new UpdateEnrollmentCoursesRequest(null, Set.of(course.id()));
        EnrollmentDTO afterRemove = mockMvc(
                MockMvcRequestBuilders.patch(ENROLLMENTS_API + "/" + created.id() + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(removeRequest)),
                EnrollmentDTO.class,
                status().isOk());
        assertThat(afterRemove.courseIds()).isEmpty();
    }

    @Test
    void shouldEndEnrollment() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        StudentDTO student = createStudent(organization.getId());
        EnrollmentDTO created = enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        EnrollmentDTO result = mockMvc(
                MockMvcRequestBuilders.post(ENROLLMENTS_API + "/" + created.id() + "/end")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:update")),
                EnrollmentDTO.class,
                status().isOk());

        assertThat(result.status()).isEqualTo(EnrollmentStatus.ENDED);
        assertThat(result.endDate()).isNotNull();
    }

    @Test
    void shouldAllowNewActiveEnrollmentAfterPreviousOneEnded() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        StudentDTO student = createStudent(organization.getId());
        EnrollmentDTO created = enroll(organization.getId(), student.id(), schoolClass.id(), Set.of());

        mockMvc.perform(MockMvcRequestBuilders.post(ENROLLMENTS_API + "/" + created.id() + "/end")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:update")))
                .andExpect(status().isOk());

        CreateEnrollmentRequest request = new CreateEnrollmentRequest(student.id(), schoolClass.id(), Set.of());
        mockMvc.perform(MockMvcRequestBuilders.post(ENROLLMENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "enrollment:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // endregion

    private ClassDTO createClass(Long organizationId) throws Exception {
        return createClass(organizationId, "CP1", OWNER_EMAIL);
    }

    private ClassDTO createClass(Long organizationId, String name) throws Exception {
        return createClass(organizationId, name, OWNER_EMAIL);
    }

    private ClassDTO createClassAs(Long organizationId, String email) throws Exception {
        return createClass(organizationId, "CP1", email);
    }

    private ClassDTO createClass(Long organizationId, String name, String email) throws Exception {
        CreateClassRequest request = new CreateClassRequest(name, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(email, organizationId, "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isCreated());
    }

    private CourseDTO createCourse(Long organizationId, Long classId) throws Exception {
        return createCourse(organizationId, classId, OWNER_EMAIL);
    }

    private CourseDTO createCourse(Long organizationId, Long classId, String email) throws Exception {
        CreateCourseRequest request = new CreateCourseRequest(
                "Qaida", CourseType.QAIDA, null, List.of("Lesson 1"), null, null, null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(email, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());
    }

    private StudentDTO createStudent(Long organizationId) throws Exception {
        return createStudent(organizationId, OWNER_EMAIL);
    }

    private StudentDTO createStudent(Long organizationId, String email) throws Exception {
        CreateStudentRequest request = new CreateStudentRequest("Aminata", "Diallo", null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(email, organizationId, "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
                status().isCreated());
    }

    private EnrollmentDTO enroll(Long organizationId, Long studentId, Long classId, Set<Long> courseIds)
            throws Exception {
        return enroll(organizationId, studentId, classId, courseIds, OWNER_EMAIL);
    }

    private EnrollmentDTO enroll(Long organizationId, Long studentId, Long classId, Set<Long> courseIds, String email)
            throws Exception {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest(studentId, classId, courseIds);
        return mockMvc(
                MockMvcRequestBuilders.post(ENROLLMENTS_API)
                        .with(authenticatedForOrganization(email, organizationId, "enrollment:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                EnrollmentDTO.class,
                status().isCreated());
    }

    private OrganizationDTO registerAsOwner(String email, String name, String contactEmail) throws Exception {
        RegisterSchoolRequest request = new RegisterSchoolRequest(name, contactEmail, null, null, null, null);
        String response = mockMvc.perform(MockMvcRequestBuilders.post(ORGANIZATION_API + "/register")
                        .with(jwt().jwt(j -> j.subject(email)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, OrganizationDTO.class);
    }

    private RequestPostProcessor authenticatedForOrganization(String email, Long organizationId, String authority) {
        return jwt().jwt(j -> j.subject(email).claim("orgId", organizationId))
                .authorities(new SimpleGrantedAuthority(authority));
    }

    private <T> T mockMvc(MockHttpServletRequestBuilder builder, TypeReference<T> typeReference, ResultMatcher matcher)
            throws Exception {
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, typeReference);
    }

    private <T> T mockMvc(MockHttpServletRequestBuilder builder, Class<T> responseType, ResultMatcher matcher)
            throws Exception {
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, responseType);
    }
}
