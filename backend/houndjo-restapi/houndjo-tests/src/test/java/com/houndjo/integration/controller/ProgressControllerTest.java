package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.FluencyRating;
import com.houndjo.domain.enumerations.ProgressFlow;
import com.houndjo.domain.enumerations.ProgressStatus;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ProgressDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateSessionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RecordProgressRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateProgressRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class ProgressControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String COURSES_API = "/api/v1/courses";
    private static final String STUDENTS_API = "/api/v1/students";
    private static final String PROGRESS_API = "/api/v1/progress";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region record

    @Test
    void shouldRecordThreeIndependentFlowsForSameSession() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        ProgressDTO sabak = recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABAK, 1, 1, 1, 7));
        ProgressDTO sabqi = recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABQI, 1, 1, 1, 7));
        ProgressDTO dhor = recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.DHOR, 1, 1, 1, 7));

        assertThat(sabak.id()).isNotEqualTo(sabqi.id());
        assertThat(sabqi.id()).isNotEqualTo(dhor.id());
        assertThat(sabak.flow()).isEqualTo(ProgressFlow.SABAK);
        assertThat(sabqi.flow()).isEqualTo(ProgressFlow.SABQI);
        assertThat(dhor.flow()).isEqualTo(ProgressFlow.DHOR);
        assertThat(sabak.quranPortion().fromSurah()).isEqualTo(1);
        assertThat(sabak.quranPortion().toVerse()).isEqualTo(7);
    }

    @Test
    void shouldRejectQuranPortionOutsideReferenceData() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        RecordProgressRequest request =
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABAK, 200, 1, 200, 1);

        mockMvc.perform(MockMvcRequestBuilders.post(PROGRESS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void shouldRejectMismatchedPortionForFlow() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());

        RecordProgressRequest request = new RecordProgressRequest(
                student.id(),
                course.id(),
                session.id(),
                ProgressFlow.SABAK,
                null,
                null,
                null,
                null,
                1L,
                null,
                null,
                0,
                FluencyRating.GOOD,
                null,
                ProgressStatus.IN_PROGRESS,
                null);

        mockMvc.perform(MockMvcRequestBuilders.post(PROGRESS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    // endregion

    // region history

    @Test
    void shouldFilterHistoryByFlow() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());
        recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABAK, 1, 1, 1, 7));
        recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABQI, 1, 1, 1, 7));

        PaginatedResult<ProgressDTO> result = mockMvc(
                MockMvcRequestBuilders.get(PROGRESS_API + "?flow=SABQI")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).flow()).isEqualTo(ProgressFlow.SABQI);
    }

    // endregion

    // region correction

    @Test
    void shouldCorrectExistingRecord() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        SessionDTO session = createSession(organization.getId(), course.id());
        ProgressDTO created = recordProgress(
                organization.getId(),
                quranRequest(student.id(), course.id(), session.id(), ProgressFlow.SABAK, 1, 1, 1, 7));

        UpdateProgressRequest request = new UpdateProgressRequest(
                1,
                1,
                1,
                5,
                null,
                null,
                null,
                2,
                FluencyRating.EXCELLENT,
                FluencyRating.GOOD,
                ProgressStatus.VALIDATED,
                "Great");

        ProgressDTO result = mockMvc(
                MockMvcRequestBuilders.put(PROGRESS_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "progress:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ProgressDTO.class,
                status().isOk());

        assertThat(result.quranPortion().toVerse()).isEqualTo(5);
        assertThat(result.errorCount()).isEqualTo(2);
        assertThat(result.status()).isEqualTo(ProgressStatus.VALIDATED);
    }

    // endregion

    // region isolation

    @Test
    void shouldNotGetProgressRecordFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClassAs(otherOrganization.getId(), otherOwner);
        CourseDTO otherCourse = createQuranCourseAs(otherOrganization.getId(), otherClass.id(), otherOwner);
        StudentDTO otherStudent = createStudentAs(otherOrganization.getId(), otherOwner);
        SessionDTO otherSession = createSessionAs(otherOrganization.getId(), otherCourse.id(), otherOwner);
        ProgressDTO otherProgress = recordProgressAs(
                otherOrganization.getId(),
                quranRequest(otherStudent.id(), otherCourse.id(), otherSession.id(), ProgressFlow.SABAK, 1, 1, 1, 7),
                otherOwner);

        mockMvc.perform(MockMvcRequestBuilders.get(PROGRESS_API + "/" + otherProgress.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "progress:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private RecordProgressRequest quranRequest(
            Long studentId,
            Long courseId,
            Long sessionId,
            ProgressFlow flow,
            int fromSurah,
            int fromVerse,
            int toSurah,
            int toVerse) {
        return new RecordProgressRequest(
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
                ProgressStatus.IN_PROGRESS,
                null);
    }

    private ProgressDTO recordProgress(Long organizationId, RecordProgressRequest request) throws Exception {
        return recordProgressAs(organizationId, request, OWNER_EMAIL);
    }

    private ProgressDTO recordProgressAs(Long organizationId, RecordProgressRequest request, String email)
            throws Exception {
        return mockMvc(
                MockMvcRequestBuilders.post(PROGRESS_API)
                        .with(authenticatedForOrganization(email, organizationId, "progress:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ProgressDTO.class,
                status().isCreated());
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

    private CourseDTO createQuranCourse(Long organizationId, Long classId) throws Exception {
        return createQuranCourseAs(organizationId, classId, OWNER_EMAIL);
    }

    private CourseDTO createQuranCourseAs(Long organizationId, Long classId, String email) throws Exception {
        CreateCourseRequest request =
                new CreateCourseRequest("Hifz", CourseType.QURAN, null, null, QuranMode.HIFZ, 1, 5, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(email, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());
    }

    private StudentDTO createStudent(Long organizationId) throws Exception {
        return createStudentAs(organizationId, OWNER_EMAIL);
    }

    private StudentDTO createStudentAs(Long organizationId, String email) throws Exception {
        CreateStudentRequest request = new CreateStudentRequest("Aminata", "Diallo", null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(email, organizationId, "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
                status().isCreated());
    }

    private SessionDTO createSession(Long organizationId, Long courseId) throws Exception {
        return createSessionAs(organizationId, courseId, OWNER_EMAIL);
    }

    private SessionDTO createSessionAs(Long organizationId, Long courseId, String email) throws Exception {
        CreateSessionRequest request = new CreateSessionRequest(LocalDate.of(2026, 3, 2), null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + courseId + "/sessions")
                        .with(authenticatedForOrganization(email, organizationId, "session:create"))
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

    private <T> T mockMvc(MockHttpServletRequestBuilder builder, TypeReference<T> typeReference, ResultMatcher matcher)
            throws Exception {
        String response = mockMvc.perform(builder)
                .andExpect(matcher)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(response, typeReference);
    }
}
