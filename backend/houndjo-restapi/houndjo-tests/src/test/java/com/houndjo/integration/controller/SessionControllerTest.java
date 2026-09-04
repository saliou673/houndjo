package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.OrganizationRole;
import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.enumerations.SessionStatus;
import com.houndjo.domain.models.membership.Membership;
import com.houndjo.domain.ports.out.persistenceport.MembershipPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateSessionRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.GenerateSessionsRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.SetPaceRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateSessionRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class SessionControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String COURSES_API = "/api/v1/courses";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    @Autowired
    private MembershipPersistencePort membershipPersistencePort;

    // region create

    @Test
    void shouldCreateSingleSession() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());

        CreateSessionRequest request = new CreateSessionRequest(LocalDate.of(2026, 3, 2), null, null, null);
        SessionDTO result = mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + course.id() + "/sessions")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                SessionDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.courseId()).isEqualTo(course.id());
        assertThat(result.sessionDate()).isEqualTo(LocalDate.of(2026, 3, 2));
        assertThat(result.status()).isEqualTo(SessionStatus.PLANNED);
    }

    @Test
    void shouldAssignActiveTeacherFromCourseOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        Long teacherId = createUser("teacher@al-nour.test").getId();
        membershipPersistencePort.save(Membership.create(teacherId, organization.getId(), OrganizationRole.TEACHER));

        CreateSessionRequest request = new CreateSessionRequest(LocalDate.of(2026, 3, 2), null, null, teacherId);
        SessionDTO result = mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + course.id() + "/sessions")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                SessionDTO.class,
                status().isCreated());

        assertThat(result.teacherUserId()).isEqualTo(teacherId);
        assertThat(result.teacherName()).isEqualTo("Mamadou Diallo");
    }

    @Test
    void shouldRejectTeacherFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwnerEmail = "owner@other-school.test";
        Long otherOwnerId = createUser(otherOwnerEmail).getId();
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        registerAsOwner(otherOwnerEmail, "Other School", "contact@other-school.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        CreateSessionRequest request = new CreateSessionRequest(LocalDate.of(2026, 3, 2), null, null, otherOwnerId);

        mockMvc.perform(MockMvcRequestBuilders.post(COURSES_API + "/" + course.id() + "/sessions")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectCrossOrganizationTeacherOnUpdate() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwnerEmail = "owner@other-school.test";
        Long otherOwnerId = createUser(otherOwnerEmail).getId();
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        registerAsOwner(otherOwnerEmail, "Other School", "contact@other-school.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        SessionDTO session = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 2));
        UpdateSessionRequest request = new UpdateSessionRequest(LocalDate.of(2026, 3, 2), null, null, otherOwnerId);

        mockMvc.perform(MockMvcRequestBuilders.put(COURSES_API + "/" + course.id() + "/sessions/" + session.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // endregion

    // region generate

    @Test
    void shouldGenerateSixSessionsOverTwoWeeksAtThreePerWeek() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        setPace(organization.getId(), course.id(), 3);

        GenerateSessionsRequest request =
                new GenerateSessionsRequest(LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 15));

        String response = mockMvc.perform(
                        MockMvcRequestBuilders.post(COURSES_API + "/" + course.id() + "/sessions/generate")
                                .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:create"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        SessionDTO[] generated = objectMapper.readValue(response, SessionDTO[].class);

        assertThat(generated).hasSize(6);
        assertThat(generated).allMatch(s -> s.status() == SessionStatus.PLANNED);
    }

    @Test
    void shouldRejectMoreThanSevenSessionsPerWeek() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        SetPaceRequest request = new SetPaceRequest(PaceUnit.CHAPTER, BigDecimal.ONE, 8, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.put(COURSES_API + "/" + course.id() + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // endregion

    // region list & get

    @Test
    void shouldFilterSessionsByDateRange() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 1));
        createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 10));

        PaginatedResult<SessionDTO> result = mockMvc(
                MockMvcRequestBuilders.get(COURSES_API + "/" + course.id() + "/sessions?fromDate=2026-03-05")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).sessionDate()).isEqualTo(LocalDate.of(2026, 3, 10));
    }

    @Test
    void shouldNotGetSessionFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClassAs(otherOrganization.getId(), otherOwner);
        CourseDTO otherCourse = createBookCourseAs(otherOrganization.getId(), otherClass.id(), otherOwner);
        SessionDTO otherSession =
                createSessionAs(otherOrganization.getId(), otherCourse.id(), LocalDate.of(2026, 3, 1), otherOwner);

        mockMvc.perform(MockMvcRequestBuilders.get(
                                COURSES_API + "/" + otherCourse.id() + "/sessions/" + otherSession.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "session:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region cancel

    @Test
    void shouldCancelSession() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        SessionDTO created = createSession(organization.getId(), course.id(), LocalDate.of(2026, 3, 1));

        SessionDTO result = mockMvc(
                MockMvcRequestBuilders.post(COURSES_API + "/" + course.id() + "/sessions/" + created.id() + "/cancel")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "session:update")),
                SessionDTO.class,
                status().isOk());

        assertThat(result.status()).isEqualTo(SessionStatus.CANCELLED);
    }

    // endregion

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

    private void setPace(Long organizationId, Long courseId, int sessionsPerWeek) throws Exception {
        SetPaceRequest request =
                new SetPaceRequest(PaceUnit.CHAPTER, new BigDecimal("1"), sessionsPerWeek, null, null, null, null);
        mockMvc.perform(MockMvcRequestBuilders.put(COURSES_API + "/" + courseId + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
