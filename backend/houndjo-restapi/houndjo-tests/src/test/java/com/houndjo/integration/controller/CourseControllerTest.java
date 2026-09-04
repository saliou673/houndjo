package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.domain.enumerations.CourseType;
import com.houndjo.domain.enumerations.QuranMode;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.CourseDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateCourseRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateCourseRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class CourseControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region create

    @Test
    void shouldCreateQuranCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request = new CreateCourseRequest(
                "Hifz Juz Amma", CourseType.QURAN, null, null, QuranMode.HIFZ, 28, 30, null, null, null);

        CourseDTO result = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.classId()).isEqualTo(classId);
        assertThat(result.type()).isEqualTo(CourseType.QURAN);
        assertThat(result.quranMode()).isEqualTo(QuranMode.HIFZ);
        assertThat(result.quranScope().fromJuz()).isEqualTo(28);
        assertThat(result.quranScope().toJuz()).isEqualTo(30);
        assertThat(result.bookTitle()).isNull();
    }

    @Test
    void shouldCreateQaidaCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request = new CreateCourseRequest(
                "Qaida niveau 1",
                CourseType.QAIDA,
                null,
                List.of("Arabic letters", "Short vowels"),
                null,
                null,
                null,
                null,
                null,
                null);

        CourseDTO result = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());

        assertThat(result.type()).isEqualTo(CourseType.QAIDA);
        assertThat(result.qaidaLessons()).containsExactly("Arabic letters", "Short vowels");
        assertThat(result.quranMode()).isNull();
        assertThat(result.bookTitle()).isNull();
    }

    @Test
    void shouldCreateBookCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request = new CreateCourseRequest(
                "Riyad as-Salihin", CourseType.BOOK, null, null, null, null, null, "Riyad as-Salihin", 372, 600);

        CourseDTO result = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());

        assertThat(result.type()).isEqualTo(CourseType.BOOK);
        assertThat(result.bookTitle()).isEqualTo("Riyad as-Salihin");
        assertThat(result.bookTotalChapters()).isEqualTo(372);
        assertThat(result.bookTotalPages()).isEqualTo(600);
        assertThat(result.quranScope()).isNull();
    }

    @Test
    void shouldRejectQuranCourseWithoutQuranMode() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Hifz", CourseType.QURAN, null, null, null, 28, 30, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectQuranCourseWithInvertedScope() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Hifz", CourseType.QURAN, null, null, QuranMode.HIFZ, 10, 2, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectQuranCourseWithNonExistentJuz() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Hifz", CourseType.QURAN, null, null, QuranMode.HIFZ, 1, 31, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectBookCourseWithoutTitle() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Book course", CourseType.BOOK, null, null, null, null, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectQaidaCourseWithoutLessons() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Qaida", CourseType.QAIDA, null, List.of(), null, null, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectBookCountsOutsideSmallintRange() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        CreateCourseRequest request =
                new CreateCourseRequest("Book", CourseType.BOOK, null, null, null, null, null, "Book", 40_000, -1);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldForbidCreateWithoutOrgRole() throws Exception {
        String noRoleEmail = "outsider@al-nour.test";
        createUser(OWNER_EMAIL);
        createUser(noRoleEmail);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        CreateCourseRequest request = new CreateCourseRequest(
                "Qaida", CourseType.QAIDA, null, List.of("Letters"), null, null, null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(noRoleEmail, organization.getId(), "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // endregion

    // region list & get

    @Test
    void shouldListCoursesOfClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        createQaidaCourse(OWNER_EMAIL, organization.getId(), classId, "Qaida 1");
        createQaidaCourse(OWNER_EMAIL, organization.getId(), classId, "Qaida 2");

        PaginatedResult<CourseDTO> result = mockMvc(
                MockMvcRequestBuilders.get(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(2);

        ClassDTO schoolClass = mockMvc(
                MockMvcRequestBuilders.get(CLASSES_API + "/" + classId)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:read")),
                ClassDTO.class,
                status().isOk());
        assertThat(schoolClass.courseCount()).isEqualTo(2);
    }

    @Test
    void shouldGetCourseById() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        CourseDTO created = createQaidaCourse(OWNER_EMAIL, organization.getId(), classId, "Qaida 1");

        CourseDTO result = mockMvc(
                MockMvcRequestBuilders.get(CLASSES_API + "/" + classId + "/courses/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:read")),
                CourseDTO.class,
                status().isOk());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo("Qaida 1");
    }

    @Test
    void shouldNotAccessCourseFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        Long otherClassId = createClass(otherOwner, otherOrganization.getId(), "CP1");
        CourseDTO otherCourse = createQaidaCourse(otherOwner, otherOrganization.getId(), otherClassId, "Qaida 1");

        mockMvc.perform(MockMvcRequestBuilders.get(CLASSES_API + "/" + otherClassId + "/courses/" + otherCourse.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "course:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region update & delete

    @Test
    void shouldUpdateCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        CourseDTO created = createQaidaCourse(OWNER_EMAIL, organization.getId(), classId, "Qaida 1");

        UpdateCourseRequest request = new UpdateCourseRequest(
                "Hifz Juz Amma", CourseType.QURAN, "updated", null, QuranMode.NAZIRA, 29, 30, null, null, null);

        CourseDTO result = mockMvc(
                MockMvcRequestBuilders.put(CLASSES_API + "/" + classId + "/courses/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isOk());

        assertThat(result.name()).isEqualTo("Hifz Juz Amma");
        assertThat(result.type()).isEqualTo(CourseType.QURAN);
        assertThat(result.quranMode()).isEqualTo(QuranMode.NAZIRA);
        assertThat(result.quranScope().fromJuz()).isEqualTo(29);
    }

    @Test
    void shouldDeleteCourse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        Long classId = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        CourseDTO created = createQaidaCourse(OWNER_EMAIL, organization.getId(), classId, "Qaida 1");

        mockMvc.perform(MockMvcRequestBuilders.delete(CLASSES_API + "/" + classId + "/courses/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get(CLASSES_API + "/" + classId + "/courses/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "course:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private CourseDTO createQaidaCourse(String email, Long organizationId, Long classId, String name) throws Exception {
        CreateCourseRequest request = new CreateCourseRequest(
                name, CourseType.QAIDA, null, List.of("Letters"), null, null, null, null, null, null);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API + "/" + classId + "/courses")
                        .with(authenticatedForOrganization(email, organizationId, "course:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                CourseDTO.class,
                status().isCreated());
    }

    private Long createClass(String email, Long organizationId, String name) throws Exception {
        CreateClassRequest request = new CreateClassRequest(name, null, null);
        ClassDTO result = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(email, organizationId, "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isCreated());
        return result.id();
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
