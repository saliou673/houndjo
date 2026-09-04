package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.StudentDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateStudentRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateStudentRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class StudentControllerTest extends IntegrationTest {

    private static final String STUDENTS_API = "/api/v1/students";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region create

    @Test
    void shouldCreateStudent() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateStudentRequest request = new CreateStudentRequest(
                "Aminata", "Diallo", LocalDate.of(2015, 3, 10), UserGender.FEMALE, "Fatoumata Diallo", "0600000000");

        StudentDTO result = mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.firstName()).isEqualTo("Aminata");
        assertThat(result.lastName()).isEqualTo("Diallo");
        assertThat(result.birthDate()).isEqualTo(LocalDate.of(2015, 3, 10));
        assertThat(result.gender()).isEqualTo(UserGender.FEMALE);
        assertThat(result.guardianName()).isEqualTo("Fatoumata Diallo");
        assertThat(result.guardianPhone()).isEqualTo("0600000000");
    }

    @Test
    void shouldNotExposeUserIdInResponse() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateStudentRequest request = new CreateStudentRequest("Aminata", "Diallo", null, null, null, null);

        JsonNode result = mockMvc(
                MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                JsonNode.class,
                status().isCreated());

        assertThat(result.has("userId")).isFalse();
    }

    @Test
    void shouldRejectCreateWithBlankFirstName() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateStudentRequest request = new CreateStudentRequest("", "Diallo", null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:create"))
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
        CreateStudentRequest request = new CreateStudentRequest("Aminata", "Diallo", null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(STUDENTS_API)
                        .with(authenticatedForOrganization(noRoleEmail, organization.getId(), "student:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // endregion

    // region list & get

    @Test
    void shouldListStudentsOfActiveOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        createStudent(OWNER_EMAIL, organization.getId(), "Aminata", "Diallo");
        createStudent(OWNER_EMAIL, organization.getId(), "Ibrahima", "Barry");

        PaginatedResult<StudentDTO> result = mockMvc(
                MockMvcRequestBuilders.get(STUDENTS_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
                .extracting(StudentDTO::firstName)
                .containsExactlyInAnyOrder("Aminata", "Ibrahima");
    }

    @Test
    void shouldSearchStudentsByName() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        createStudent(OWNER_EMAIL, organization.getId(), "Aminata", "Diallo");
        createStudent(OWNER_EMAIL, organization.getId(), "Ibrahima", "Barry");

        PaginatedResult<StudentDTO> result = mockMvc(
                MockMvcRequestBuilders.get(STUDENTS_API + "?search=dial")
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).lastName()).isEqualTo("Diallo");
    }

    @Test
    void shouldGetStudentById() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        StudentDTO created = createStudent(OWNER_EMAIL, organization.getId(), "Aminata", "Diallo");

        StudentDTO result = mockMvc(
                MockMvcRequestBuilders.get(STUDENTS_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:read")),
                StudentDTO.class,
                status().isOk());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.firstName()).isEqualTo("Aminata");
    }

    @Test
    void shouldNotGetStudentFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        StudentDTO otherStudent = createStudent(otherOwner, otherOrganization.getId(), "Aminata", "Diallo");

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_API + "/" + otherStudent.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "student:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region update

    @Test
    void shouldUpdateStudent() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        StudentDTO created = createStudent(OWNER_EMAIL, organization.getId(), "Aminata", "Diallo");
        UpdateStudentRequest request = new UpdateStudentRequest(
                "Aminata", "Diallo Bah", LocalDate.of(2014, 1, 1), UserGender.FEMALE, "Fatoumata Diallo", "0611111111");

        StudentDTO result = mockMvc(
                MockMvcRequestBuilders.put(STUDENTS_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                StudentDTO.class,
                status().isOk());

        assertThat(result.lastName()).isEqualTo("Diallo Bah");
        assertThat(result.birthDate()).isEqualTo(LocalDate.of(2014, 1, 1));
        assertThat(result.guardianPhone()).isEqualTo("0611111111");
    }

    @Test
    void shouldNotUpdateStudentFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        StudentDTO otherStudent = createStudent(otherOwner, otherOrganization.getId(), "Aminata", "Diallo");
        UpdateStudentRequest request = new UpdateStudentRequest("Hacked", "Hacked", null, null, null, null);

        mockMvc.perform(MockMvcRequestBuilders.put(STUDENTS_API + "/" + otherStudent.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "student:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region delete

    @Test
    void shouldDeleteStudent() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        StudentDTO created = createStudent(OWNER_EMAIL, organization.getId(), "Aminata", "Diallo");

        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENTS_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get(STUDENTS_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "student:read")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteStudentFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        StudentDTO otherStudent = createStudent(otherOwner, otherOrganization.getId(), "Aminata", "Diallo");

        mockMvc.perform(MockMvcRequestBuilders.delete(STUDENTS_API + "/" + otherStudent.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "student:delete")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private StudentDTO createStudent(String email, Long organizationId, String firstName, String lastName)
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
