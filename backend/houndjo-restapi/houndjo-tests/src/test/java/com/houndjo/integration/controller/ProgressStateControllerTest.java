package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PaceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressStateDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.RevisionAlertDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateSessionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RecordProgressRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.SetPaceRequest;
import com.houndjo.integration.IntegrationTest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class ProgressStateControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String COURSES_API = "/api/v1/courses";
    private static final String STUDENTS_API = "/api/v1/students";
    private static final String PROGRESS_API = "/api/v1/progress";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String ORGANIZATIONS_V1_API = "/api/v1/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";
    private static final int DHOR_CYCLE_DAYS = 30;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFlagDhorPastThreshold() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        setPace(organization.getId(), course.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        ProgressDTO sabak = recordProgress(
                organization.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.SABAK,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        ProgressDTO dhor = recordProgress(
                organization.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        forceCreationDate(dhor.id(), Instant.now().minus(DHOR_CYCLE_DAYS + 5L, ChronoUnit.DAYS));

        ProgressStateDTO state = getProgressState(organization.getId(), student.id(), course.id());

        assertThat(state.sabak()).isNotNull();
        assertThat(state.sabak().fromSurah()).isEqualTo(1);
        assertThat(state.coveredJuz()).contains(1);
        assertThat(state.stalePortions()).isNotEmpty();
        assertThat(state.stalePortions().get(0).juz()).isEqualTo(1);
        assertThat(state.alerts()).hasSize(state.stalePortions().size());
        assertThat(sabak.id()).isNotNull();
    }

    @Test
    void shouldNotFlagDhorWithinThreshold() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        setPace(organization.getId(), course.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        recordProgress(
                organization.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);

        ProgressStateDTO state = getProgressState(organization.getId(), student.id(), course.id());

        assertThat(state.coveredJuz()).contains(1);
        assertThat(state.stalePortions()).isEmpty();
        assertThat(state.alerts()).isEmpty();
    }

    @Test
    void shouldFlagStaleDhorEvenWithGoodSabak() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        setPace(organization.getId(), course.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        recordProgress(
                organization.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.SABAK,
                2,
                1,
                2,
                10,
                ProgressStatus.VALIDATED);
        ProgressDTO dhor = recordProgress(
                organization.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        forceCreationDate(dhor.id(), Instant.now().minus(DHOR_CYCLE_DAYS + 10L, ChronoUnit.DAYS));

        ProgressStateDTO state = getProgressState(organization.getId(), student.id(), course.id());

        assertThat(state.sabak().fromSurah()).isEqualTo(2);
        assertThat(state.stalePortions()).anyMatch(portion -> portion.juz() == 1);
    }

    @Test
    void shouldRejectProgressStateForNonQuranCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CreateCourseRequest bookRequest =
                new CreateCourseRequest("Fiqh", CourseType.BOOK, null, null, null, null, null, "Fiqh Book", 10, 100);
        CourseDTO bookCourse = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + schoolClass.id() + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookRequest)),
                CourseDTO.class,
                status().isCreated());
        StudentDTO student = createStudent(organization.getId());

        mockMvc.perform(MockMvcRequestBuilders.get(
                                STUDENTS_API + "/" + student.id() + "/progress-state?courseId=" + bookCourse.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:read")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAggregateRevisionAlertsForOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        setPace(organization.getId(), course.id());
        StudentDTO staleStudent = createStudent(organization.getId());
        StudentDTO upToDateStudent = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        ProgressDTO staleDhor = recordProgress(
                organization.getId(),
                staleStudent.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        forceCreationDate(staleDhor.id(), Instant.now().minus(DHOR_CYCLE_DAYS + 5L, ChronoUnit.DAYS));
        recordProgress(
                organization.getId(),
                upToDateStudent.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);

        RevisionAlertDTO[] alerts = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATIONS_V1_API + "/" + organization.getId() + "/revision-alerts")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:read")),
                RevisionAlertDTO[].class,
                status().isOk());

        assertThat(alerts).extracting(RevisionAlertDTO::studentId).containsExactly(staleStudent.id());
    }

    @Test
    void partialDhorMustKeepAlertForUnreviewedVerses() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO org = registerAsOwner(OWNER_EMAIL, "School", "contact@school.test");
        CourseDTO course =
                createQuranCourse(org.getId(), createClass(org.getId()).id());
        setPace(org.getId(), course.id());
        StudentDTO student = createStudent(org.getId());
        SessionDTO session = createSession(org.getId(), course.id());
        ProgressDTO old = recordProgress(
                org.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        forceCreationDate(old.id(), Instant.now().minus(40, ChronoUnit.DAYS));
        recordProgress(
                org.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                1,
                1,
                3,
                ProgressStatus.VALIDATED);
        assertThat(getProgressState(org.getId(), student.id(), course.id()).stalePortions())
                .hasSize(1);
        RevisionAlertDTO[] alerts = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATIONS_V1_API + "/" + org.getId() + "/revision-alerts")
                        .with(authenticatedForOrganization(OWNER_EMAIL, org.getId(), "progress:read")),
                RevisionAlertDTO[].class,
                status().isOk());
        assertThat(alerts).extracting(RevisionAlertDTO::studentId).containsExactly(student.id());
        recordProgress(
                org.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.DHOR,
                1,
                4,
                1,
                7,
                ProgressStatus.VALIDATED);
        assertThat(getProgressState(org.getId(), student.id(), course.id()).stalePortions())
                .isEmpty();
    }

    @Test
    void shouldAlertForOldMemorizationNeverReviewedInDhor() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO org = registerAsOwner(OWNER_EMAIL, "School", "contact@school.test");
        CourseDTO course =
                createQuranCourse(org.getId(), createClass(org.getId()).id());
        setPace(org.getId(), course.id());
        StudentDTO student = createStudent(org.getId());
        SessionDTO session = createSession(org.getId(), course.id());
        ProgressDTO old = recordProgress(
                org.getId(),
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.SABAK,
                1,
                1,
                1,
                7,
                ProgressStatus.VALIDATED);
        forceCreationDate(old.id(), Instant.now().minus(40, ChronoUnit.DAYS));
        ProgressStateDTO state = getProgressState(org.getId(), student.id(), course.id());
        assertThat(state.coveredJuz()).isEmpty();
        assertThat(state.stalePortions()).singleElement().satisfies(stale -> {
            assertThat(stale.lastReviewedDate()).isNull();
            assertThat(stale.daysSince()).isGreaterThanOrEqualTo(39);
        });
    }

    @Test
    void shouldIsolateStateAndRevisionAlertsByOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "other@school.test";
        createUser(otherOwner);
        OrganizationDTO org = registerAsOwner(OWNER_EMAIL, "School", "contact@school.test");
        OrganizationDTO other = registerAsOwner(otherOwner, "Other", "contact@other.test");
        CourseDTO course =
                createQuranCourse(org.getId(), createClass(org.getId()).id());
        StudentDTO student = createStudent(org.getId());
        mockMvc.perform(MockMvcRequestBuilders.get(
                                STUDENTS_API + "/" + student.id() + "/progress-state?courseId=" + course.id())
                        .with(authenticatedForOrganization(otherOwner, other.getId(), "progress:read")))
                .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.get(ORGANIZATIONS_V1_API + "/" + org.getId() + "/revision-alerts")
                        .with(authenticatedForOrganization(otherOwner, other.getId(), "progress:read")))
                .andExpect(status().isNotFound());
        RevisionAlertDTO[] alerts = mockMvc(
                MockMvcRequestBuilders.get(ORGANIZATIONS_V1_API + "/" + other.getId() + "/revision-alerts")
                        .with(authenticatedForOrganization(otherOwner, other.getId(), "progress:read")),
                RevisionAlertDTO[].class,
                status().isOk());
        assertThat(alerts).isEmpty();
    }

    private void forceCreationDate(Long progressRecordId, Instant creationDate) {
        int updated = jdbcTemplate.update(
                "UPDATE progress_record SET creation_date = ? WHERE id = ?",
                Timestamp.from(creationDate),
                progressRecordId);
        if (updated != 1) {
            throw new IllegalStateException("Expected 1 row updated, got " + updated);
        }
    }

    private ProgressStateDTO getProgressState(Long organizationId, Long studentId, Long courseId) throws Exception {
        return mockMvc(
                MockMvcRequestBuilders.get(STUDENTS_API + "/" + studentId + "/progress-state?courseId=" + courseId)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "progress:read")),
                ProgressStateDTO.class,
                status().isOk());
    }

    private ProgressDTO recordProgress(
            Long organizationId,
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            int fromSurah,
            int fromVerse,
            int toSurah,
            int toVerse,
            ProgressStatus status)
            throws Exception {
        RecordProgressRequest request = new RecordProgressRequest(
                studentId,
                courseId,
                sessionId,
                flow,
                fromSurah,
                fromVerse,
                toSurah,
                toVerse,
                null,
                null,
                null,
                0,
                FluencyRating.GOOD,
                FluencyRating.FAIR,
                status,
                null);
        return mockMvc(
                MockMvcRequestBuilders.post(PROGRESS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "progress:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ProgressDTO.class,
                status().isCreated());
    }

    private void setPace(Long organizationId, Long courseId) throws Exception {
        SetPaceRequest request = new SetPaceRequest(
                PaceUnit.PAGE,
                new BigDecimal("1"),
                3,
                new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("1")),
                new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("0.5")),
                new SetPaceRequest.FlowRequest(PaceUnit.HIZB, new BigDecimal("1")),
                DHOR_CYCLE_DAYS);
        mockMvc(
                MockMvcRequestBuilders.put(COURSES_API + "/" + courseId + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                PaceDTO.class,
                status().isOk());
    }

    private ClassDTO createClass(Long organizationId) throws Exception {
        CreateClassRequest request = new CreateClassRequest("CP1", null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isCreated());
    }

    private CourseDTO createQuranCourse(Long organizationId, Long classId) throws Exception {
        CreateCourseRequest request =
                new CreateCourseRequest("Hifz", CourseType.QURAN, null, null, QuranMode.HIFZ, 1, 5, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());
    }

    private StudentDTO createStudent(Long organizationId) throws Exception {
        CreateStudentRequest request = new CreateStudentRequest("Aminata", "Diallo", null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
                status().isCreated());
    }

    private SessionDTO createSession(Long organizationId, Long courseId) throws Exception {
        CreateSessionRequest request = new CreateSessionRequest(LocalDate.of(2026, 3, 2), null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + courseId + "/sessions")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "session:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                SessionDTO.class,
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
