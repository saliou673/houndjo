package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.PaceUnit;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.domain.models.quran.QuranPortion;
import com.houndjo.domain.ports.out.persistenceport.QuranReferencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PaceDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.PortionDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.SetPaceRequest;
import com.houndjo.integration.IntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class CoursePaceControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String COURSES_API = "/api/v1/courses";
    private static final String STUDENTS_API = "/api/v1/students";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    @Autowired
    private QuranReferencePort quranReferencePort;

    // region set pace

    @Test
    void shouldSetQuranPaceWithThreeFlows() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id(), 1, 5);

        SetPaceRequest request = new SetPaceRequest(
                PaceUnit.PAGE,
                new BigDecimal("1"),
                3,
                new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("1")),
                new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("0.5")),
                new SetPaceRequest.FlowRequest(PaceUnit.HIZB, new BigDecimal("1")),
                30);

        PaceDTO result = mockMvc(
                MockMvcRequestBuilders.put(COURSES_API + "/" + course.id() + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                PaceDTO.class,
                status().isOk());

        assertThat(result.courseId()).isEqualTo(course.id());
        assertThat(result.sabak().unit()).isEqualTo(PaceUnit.PAGE);
        assertThat(result.sabqi().amount()).isEqualByComparingTo("0.5");
        assertThat(result.dhor().unit()).isEqualTo(PaceUnit.HIZB);
        assertThat(result.dhorCycleDays()).isEqualTo(30);
    }

    @Test
    void shouldRejectQuranPaceMissingAFlow() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id(), 1, 5);

        SetPaceRequest request = new SetPaceRequest(
                PaceUnit.PAGE,
                new BigDecimal("1"),
                3,
                new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("1")),
                null,
                null,
                null);

        mockMvc.perform(MockMvcRequestBuilders.put(COURSES_API + "/" + course.id() + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldSetBookPaceInChapters() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());

        SetPaceRequest request = new SetPaceRequest(PaceUnit.CHAPTER, new BigDecimal("1"), 2, null, null, null, null);

        PaceDTO result = mockMvc(
                MockMvcRequestBuilders.put(COURSES_API + "/" + course.id() + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                PaceDTO.class,
                status().isOk());

        assertThat(result.unit()).isEqualTo(PaceUnit.CHAPTER);
        assertThat(result.amountPerSession()).isEqualByComparingTo("1");
        assertThat(result.sabak()).isNull();
        assertThat(result.dhorCycleDays()).isNull();
    }

    // endregion

    // region next portion

    @Test
    void shouldComputeNextPortionForPageUnit() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createQuranCourse(organization.getId(), schoolClass.id(), 1, 5);
        StudentDTO student = createStudent(organization.getId());
        setPace(
                organization.getId(),
                course.id(),
                new SetPaceRequest(
                        PaceUnit.PAGE,
                        new BigDecimal("1"),
                        3,
                        new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("1")),
                        new SetPaceRequest.FlowRequest(PaceUnit.PAGE, new BigDecimal("0.5")),
                        new SetPaceRequest.FlowRequest(PaceUnit.HIZB, new BigDecimal("1")),
                        30));

        PortionDTO result = mockMvc(
                MockMvcRequestBuilders.get(COURSES_API + "/" + course.id() + "/pace/next-portion?studentId="
                                + student.id() + "&flow=SABAK")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:read")),
                PortionDTO.class,
                status().isOk());

        QuranPortion expected = quranReferencePort.portionForPageRange(1, 1);
        assertThat(result.fromSurah()).isEqualTo(expected.fromSurah());
        assertThat(result.fromVerse()).isEqualTo(expected.fromVerse());
        assertThat(result.toSurah()).isEqualTo(expected.toSurah());
        assertThat(result.toVerse()).isEqualTo(expected.toVerse());
        assertThat(result.fromPage()).isEqualTo(1);
        assertThat(result.toPage()).isEqualTo(1);
    }

    @Test
    void shouldRejectNextPortionForNonQuranCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO schoolClass = createClass(organization.getId());
        CourseDTO course = createBookCourse(organization.getId(), schoolClass.id());
        StudentDTO student = createStudent(organization.getId());
        setPace(
                organization.getId(),
                course.id(),
                new SetPaceRequest(PaceUnit.CHAPTER, new BigDecimal("1"), 2, null, null, null, null));

        mockMvc.perform(MockMvcRequestBuilders.get(COURSES_API + "/" + course.id() + "/pace/next-portion?studentId="
                                + student.id() + "&flow=SABAK")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:read")))
                .andExpect(status().isBadRequest());
    }

    // endregion

    // region isolation

    @Test
    void shouldNotGetPaceFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClassAs(otherOrganization.getId(), otherOwner);
        CourseDTO otherCourse = createBookCourseAs(otherOrganization.getId(), otherClass.id(), otherOwner);
        setPaceAs(
                otherOrganization.getId(),
                otherCourse.id(),
                new SetPaceRequest(PaceUnit.CHAPTER, new BigDecimal("1"), 2, null, null, null, null),
                otherOwner);

        mockMvc.perform(MockMvcRequestBuilders.get(COURSES_API + "/" + otherCourse.id() + "/pace")
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "course:read")))
                .andExpect(status().isNotFound());
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

    private CourseDTO createQuranCourse(Long organizationId, Long classId, int fromJuz, int toJuz) throws Exception {
        CreateCourseRequest request = new CreateCourseRequest(
                "Hifz", CourseType.QURAN, null, null, QuranMode.HIFZ, fromJuz, toJuz, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
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

    private PaceDTO setPace(Long organizationId, Long courseId, SetPaceRequest request) throws Exception {
        return setPaceAs(organizationId, courseId, request, OWNER_EMAIL);
    }

    private PaceDTO setPaceAs(Long organizationId, Long courseId, SetPaceRequest request, String email)
            throws Exception {
        return mockMvc(
                MockMvcRequestBuilders.put(COURSES_API + "/" + courseId + "/pace")
                        .with(authenticatedForOrganization(email, organizationId, "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                PaceDTO.class,
                status().isOk());
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
