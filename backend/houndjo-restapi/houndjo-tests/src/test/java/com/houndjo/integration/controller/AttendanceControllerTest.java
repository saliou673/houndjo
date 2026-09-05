package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.AttendancePermissionStatus;
import com.houndjo.domain.enumerations.AttendanceStatus;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendanceHistoryDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AttendancePermissionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.AttendanceEntryRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.BulkAttendanceRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateAttendancePermissionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateSessionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateAttendancePermissionStatusRequest;
import com.houndjo.integration.IntegrationTest;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class AttendanceControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String COURSES_API = "/api/v1/courses";
    private static final String SESSIONS_API = "/api/v1/sessions";
    private static final String STUDENTS_API = "/api/v1/students";
    private static final String PERMISSIONS_API = "/api/v1/attendance-permissions";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region bulk roll call

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldUseLastDuplicateEntryForNewAndExistingAttendance(boolean existing) throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO org = registerAsOwner(OWNER_EMAIL, "School", "contact@school.test");
        CourseDTO course =
                createBookCourse(org.getId(), createClass(org.getId()).id());
        SessionDTO session = createSession(org.getId(), course.id(), LocalDate.of(2026, 3, 2));
        StudentDTO student = createStudent(org.getId(), "Aminata", "Diallo");
        if (existing)
            recordBulkAttendance(
                    org.getId(),
                    session.id(),
                    List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.PRESENT, null)));
        recordBulkAttendance(
                org.getId(),
                session.id(),
                List.of(
                        new AttendanceEntryRequest(student.id(), AttendanceStatus.PRESENT, null),
                        new AttendanceEntryRequest(student.id(), AttendanceStatus.ABSENT_JUSTIFIED, "Sick")));
        AttendanceDTO[] result = mockMvc(
                MockMvcRequestBuilders.get(SESSIONS_API + "/" + session.id() + "/attendance")
                        .with(authenticatedForOrganization(OWNER_EMAIL, org.getId(), "attendance:read")),
                AttendanceDTO[].class,
                status().isOk());
        assertThat(result).singleElement().satisfies(entry -> {
            assertThat(entry.status()).isEqualTo(AttendanceStatus.ABSENT_JUSTIFIED);
            assertThat(entry.reason()).isEqualTo("Sick");
        });
    }

    @Test
    void shouldRecordBulkAttendanceForSession() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        SessionDTO session = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 2));
        StudentDTO aminata = createStudent(organization.getId(), "Aminata", "Diallo");
        StudentDTO mamadou = createStudent(organization.getId(), "Mamadou", "Bah");

        BulkAttendanceRequest request = new BulkAttendanceRequest(List.of(
                new AttendanceEntryRequest(aminata.id(), AttendanceStatus.PRESENT, null),
                new AttendanceEntryRequest(mamadou.id(), AttendanceStatus.ABSENT_UNJUSTIFIED, "No news")));

        AttendanceDTO[] result = mockMvc(
                MockMvcRequestBuilders.post(SESSIONS_API + "/" + session.id() + "/attendance/bulk")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                AttendanceDTO[].class,
                status().isOk());

        assertThat(result).hasSize(2);
        AttendanceDTO aminataAttendance = findByStudentId(result, aminata.id());
        assertThat(aminataAttendance.status()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(aminataAttendance.studentName()).isEqualTo("Aminata Diallo");
        assertThat(aminataAttendance.sessionDate()).isEqualTo(session.sessionDate());
        AttendanceDTO mamadouAttendance = findByStudentId(result, mamadou.id());
        assertThat(mamadouAttendance.status()).isEqualTo(AttendanceStatus.ABSENT_UNJUSTIFIED);
        assertThat(mamadouAttendance.reason()).isEqualTo("No news");
    }

    @Test
    void shouldUpdateExistingAttendanceOnRepostWithoutDuplicating() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        SessionDTO session = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 2));
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");
        recordBulkAttendance(
                organization.getId(),
                session.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.ABSENT_UNJUSTIFIED, null)));

        recordBulkAttendance(
                organization.getId(),
                session.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.PRESENT, null)));

        AttendanceDTO[] result = mockMvc(
                MockMvcRequestBuilders.get(SESSIONS_API + "/" + session.id() + "/attendance")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:read")),
                AttendanceDTO[].class,
                status().isOk());

        assertThat(result).hasSize(1);
        assertThat(result[0].status()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    void shouldNotRecordAttendanceForAnotherOrganizationSession() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwnerEmail = "owner@other-school.test";
        createUser(otherOwnerEmail);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization =
                registerAsOwner(otherOwnerEmail, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClassAs(otherOrganization.getId(), otherOwnerEmail);
        CourseDTO otherCourse = createBookCourseAs(otherOrganization.getId(), otherClass.id(), otherOwnerEmail);
        SessionDTO otherSession =
                createSessionAs(otherOrganization.getId(), otherCourse.id(), LocalDate.of(2026, 3, 2), otherOwnerEmail);
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");

        BulkAttendanceRequest request = new BulkAttendanceRequest(
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.PRESENT, null)));

        mockMvc.perform(MockMvcRequestBuilders.post(SESSIONS_API + "/" + otherSession.id() + "/attendance/bulk")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region history

    @Test
    void shouldComputeStudentAttendanceHistoryAndAbsenceRate() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");
        SessionDTO session1 = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 1));
        SessionDTO session2 = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 8));
        SessionDTO session3 = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 15));
        recordBulkAttendance(
                organization.getId(),
                session1.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.PRESENT, null)));
        recordBulkAttendance(
                organization.getId(),
                session2.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.ABSENT_UNJUSTIFIED, null)));
        recordBulkAttendance(
                organization.getId(),
                session3.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.ABSENT_JUSTIFIED, "Sick")));

        AttendanceHistoryDTO history = mockMvc(
                MockMvcRequestBuilders.get(
                                STUDENTS_API + "/" + student.id() + "/attendance" + "?from=2026-03-01&to=2026-03-31")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:read")),
                AttendanceHistoryDTO.class,
                status().isOk());

        assertThat(history.entries()).hasSize(3);
        assertThat(history.absenceRate()).isEqualTo(2.0 / 3.0);
    }

    // endregion

    // region attendance permission

    @Test
    void shouldCreateAndApproveAttendancePermissionCoveringASession() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");
        SessionDTO session = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 10));

        CreateAttendancePermissionRequest request = new CreateAttendancePermissionRequest(
                student.id(), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 12), "Family trip");
        AttendancePermissionDTO created = mockMvc(
                MockMvcRequestBuilders.post(PERMISSIONS_API)
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, organization.getId(), "attendance-permission:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                AttendancePermissionDTO.class,
                status().isCreated());

        assertThat(created.status()).isEqualTo(AttendancePermissionStatus.PENDING);
        assertThat(created.fromDate()).isBeforeOrEqualTo(session.sessionDate());
        assertThat(created.toDate()).isAfterOrEqualTo(session.sessionDate());

        UpdateAttendancePermissionStatusRequest approve =
                new UpdateAttendancePermissionStatusRequest(AttendancePermissionStatus.APPROVED);
        AttendancePermissionDTO approved = mockMvc(
                MockMvcRequestBuilders.patch(PERMISSIONS_API + "/" + created.id())
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, organization.getId(), "attendance-permission:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(approve)),
                AttendancePermissionDTO.class,
                status().isOk());

        assertThat(approved.status()).isEqualTo(AttendancePermissionStatus.APPROVED);
        AttendanceDTO[] beforeRollCall = mockMvc(
                MockMvcRequestBuilders.get(SESSIONS_API + "/" + session.id() + "/attendance")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:read")),
                AttendanceDTO[].class,
                status().isOk());
        assertThat(beforeRollCall).isEmpty();
        recordBulkAttendance(
                organization.getId(),
                session.id(),
                List.of(new AttendanceEntryRequest(student.id(), AttendanceStatus.PERMISSION, "Family trip")));
        AttendanceHistoryDTO history = mockMvc(
                MockMvcRequestBuilders.get(STUDENTS_API + "/" + student.id() + "/attendance")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "attendance:read")),
                AttendanceHistoryDTO.class,
                status().isOk());
        assertThat(history.entries())
                .singleElement()
                .satisfies(entry -> assertThat(entry.status()).isEqualTo(AttendanceStatus.PERMISSION));
        assertThat(history.absenceRate()).isEqualTo(1.0);
    }

    @Test
    void shouldListAttendancePermissionsByStudent() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");
        StudentDTO otherStudent = createStudent(organization.getId(), "Mamadou", "Bah");
        createAttendancePermission(
                organization.getId(), student.id(), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 12));
        createAttendancePermission(
                organization.getId(), otherStudent.id(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 2));

        AttendancePermissionDTO[] result = mockMvc(
                MockMvcRequestBuilders.get(PERMISSIONS_API + "?studentId=" + student.id())
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, organization.getId(), "attendance-permission:read")),
                AttendancePermissionDTO[].class,
                status().isOk());

        assertThat(result).hasSize(1);
        assertThat(result[0].studentId()).isEqualTo(student.id());
    }

    @Test
    void shouldRejectInvalidAttendancePermissionDateRange() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        StudentDTO student = createStudent(organization.getId(), "Aminata", "Diallo");

        CreateAttendancePermissionRequest request = new CreateAttendancePermissionRequest(
                student.id(), LocalDate.of(2026, 3, 12), LocalDate.of(2026, 3, 9), null);

        mockMvc.perform(MockMvcRequestBuilders.post(PERMISSIONS_API)
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, organization.getId(), "attendance-permission:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotGetAttendancePermissionFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwnerEmail = "owner@other-school.test";
        createUser(otherOwnerEmail);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization =
                registerAsOwner(otherOwnerEmail, "Other School", "contact@other-school.test");
        StudentDTO otherStudent = createStudentAs(otherOrganization.getId(), "Aminata", "Diallo", otherOwnerEmail);
        CreateAttendancePermissionRequest request = new CreateAttendancePermissionRequest(
                otherStudent.id(), LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 12), null);
        AttendancePermissionDTO otherPermission = mockMvc(
                MockMvcRequestBuilders.post(PERMISSIONS_API)
                        .with(authenticatedForOrganization(
                                otherOwnerEmail, otherOrganization.getId(), "attendance-permission:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                AttendancePermissionDTO.class,
                status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get(PERMISSIONS_API + "/" + otherPermission.id())
                        .with(authenticatedForOrganization(
                                OWNER_EMAIL, organization.getId(), "attendance-permission:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private AttendanceDTO findByStudentId(AttendanceDTO[] attendances, Long studentId) {
        return List.of(attendances).stream()
                .filter(a -> a.studentId().equals(studentId))
                .findFirst()
                .orElseThrow();
    }

    private void recordBulkAttendance(Long organizationId, Long sessionId, List<AttendanceEntryRequest> entries)
            throws Exception {
        BulkAttendanceRequest request = new BulkAttendanceRequest(entries);
        mockMvc.perform(MockMvcRequestBuilders.post(SESSIONS_API + "/" + sessionId + "/attendance/bulk")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "attendance:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private void createAttendancePermission(Long organizationId, Long studentId, LocalDate fromDate, LocalDate toDate)
            throws Exception {
        CreateAttendancePermissionRequest request =
                new CreateAttendancePermissionRequest(studentId, fromDate, toDate, null);
        mockMvc.perform(MockMvcRequestBuilders.post(PERMISSIONS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "attendance-permission:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    private ClassDTO createClass(Long organizationId) throws Exception {
        return createClassAs(organizationId, OWNER_EMAIL);
    }

    private ClassDTO createClassAs(Long organizationId, String email) throws Exception {
        CreateClassRequest request = new CreateClassRequest("CP1", null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(email, organizationId, "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isCreated());
    }

    private CourseDTO createBookCourse(Long organizationId, Long classId) throws Exception {
        return createBookCourseAs(organizationId, classId, OWNER_EMAIL);
    }

    private CourseDTO createBookCourseAs(Long organizationId, Long classId, String email) throws Exception {
        CreateCourseRequest request =
                new CreateCourseRequest("Fiqh", CourseType.BOOK, null, null, null, null, null, "Fiqh Book", 10, 100);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(email, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());
    }

    private SessionDTO createSession(Long organizationId, Long courseId, LocalDate sessionDate) throws Exception {
        return createSessionAs(organizationId, courseId, sessionDate, OWNER_EMAIL);
    }

    private SessionDTO createSessionAs(Long organizationId, Long courseId, LocalDate sessionDate, String email)
            throws Exception {
        CreateSessionRequest request = new CreateSessionRequest(sessionDate, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + courseId + "/sessions")
                        .with(authenticatedForOrganization(email, organizationId, "session:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                SessionDTO.class,
                status().isCreated());
    }

    private StudentDTO createStudent(Long organizationId, String firstName, String lastName) throws Exception {
        return createStudentAs(organizationId, firstName, lastName, OWNER_EMAIL);
    }

    private StudentDTO createStudentAs(Long organizationId, String firstName, String lastName, String email)
            throws Exception {
        CreateStudentRequest request = new CreateStudentRequest(firstName, lastName, null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(email, organizationId, "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
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
