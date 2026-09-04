package com.houndjo.integration.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ClassDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.OrganizationDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.CreateClassRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.RegisterSchoolRequest;
import com.houndjo.infrastructure.adapter.in.rest.controller.requests.UpdateClassRequest;
import com.houndjo.infrastructure.adapter.out.query.PaginatedResult;
import com.houndjo.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

class SchoolClassControllerTest extends IntegrationTest {

    private static final String CLASSES_API = "/api/v1/classes";
    private static final String ORGANIZATION_API = "/api/organizations";
    private static final String OWNER_EMAIL = "owner@al-nour.test";

    // region create

    @Test
    void shouldCreateClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateClassRequest request = new CreateClassRequest("CP1", "Cours préparatoire 1", 1);

        ClassDTO result = mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isCreated());

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("CP1");
        assertThat(result.description()).isEqualTo("Cours préparatoire 1");
        assertThat(result.displayOrder()).isEqualTo(1);
        assertThat(result.courseCount()).isZero();
    }

    @Test
    void shouldRejectCreateWithBlankName() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        CreateClassRequest request = new CreateClassRequest("", null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:create"))
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
        CreateClassRequest request = new CreateClassRequest("CP1", null, null);

        mockMvc.perform(MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(noRoleEmail, organization.getId(), "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // endregion

    // region list & get

    @Test
    void shouldListClassesOfActiveOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        createClass(OWNER_EMAIL, organization.getId(), "CP1", 2);
        createClass(OWNER_EMAIL, organization.getId(), "CP2", 1);

        PaginatedResult<ClassDTO> result = mockMvc(
                MockMvcRequestBuilders.get(CLASSES_API)
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:read")),
                new TypeReference<>() {},
                status().isOk());

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).extracting(ClassDTO::name).containsExactly("CP2", "CP1");
    }

    @Test
    void shouldGetClassById() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO created = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        ClassDTO result = mockMvc(
                MockMvcRequestBuilders.get(CLASSES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:read")),
                ClassDTO.class,
                status().isOk());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.name()).isEqualTo("CP1");
    }

    @Test
    void shouldNotGetClassFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClass(otherOwner, otherOrganization.getId(), "CP1");

        mockMvc.perform(MockMvcRequestBuilders.get(CLASSES_API + "/" + otherClass.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "class:read")))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region update

    @Test
    void shouldUpdateClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO created = createClass(OWNER_EMAIL, organization.getId(), "CP1");
        UpdateClassRequest request = new UpdateClassRequest("CP1 Bis", "Updated description", 2);

        ClassDTO result = mockMvc(
                MockMvcRequestBuilders.put(CLASSES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
                status().isOk());

        assertThat(result.name()).isEqualTo("CP1 Bis");
        assertThat(result.description()).isEqualTo("Updated description");
        assertThat(result.displayOrder()).isEqualTo(2);
    }

    @Test
    void shouldNotUpdateClassFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClass(otherOwner, otherOrganization.getId(), "CP1");
        UpdateClassRequest request = new UpdateClassRequest("Hacked", null, null);

        mockMvc.perform(MockMvcRequestBuilders.put(CLASSES_API + "/" + otherClass.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "class:update"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // endregion

    // region delete

    @Test
    void shouldDeleteClass() throws Exception {
        createUser(OWNER_EMAIL);
        OrganizationDTO organization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        ClassDTO created = createClass(OWNER_EMAIL, organization.getId(), "CP1");

        mockMvc.perform(MockMvcRequestBuilders.delete(CLASSES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:delete")))
                .andExpect(status().isNoContent());

        mockMvc.perform(MockMvcRequestBuilders.get(CLASSES_API + "/" + created.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, organization.getId(), "class:read")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotDeleteClassFromAnotherOrganization() throws Exception {
        createUser(OWNER_EMAIL);
        String otherOwner = "owner@other-school.test";
        createUser(otherOwner);
        OrganizationDTO activeOrganization = registerAsOwner(OWNER_EMAIL, "Ecole Al Nour", "contact@al-nour.test");
        OrganizationDTO otherOrganization = registerAsOwner(otherOwner, "Other School", "contact@other-school.test");
        ClassDTO otherClass = createClass(otherOwner, otherOrganization.getId(), "CP1");

        mockMvc.perform(MockMvcRequestBuilders.delete(CLASSES_API + "/" + otherClass.id())
                        .with(authenticatedForOrganization(OWNER_EMAIL, activeOrganization.getId(), "class:delete")))
                .andExpect(status().isNotFound());
    }

    // endregion

    private ClassDTO createClass(String email, Long organizationId, String name) throws Exception {
        return createClass(email, organizationId, name, null);
    }

    private ClassDTO createClass(String email, Long organizationId, String name, Integer displayOrder)
            throws Exception {
        CreateClassRequest request = new CreateClassRequest(name, null, displayOrder);
        return mockMvc(
                MockMvcRequestBuilders.post(CLASSES_API)
                        .with(authenticatedForOrganization(email, organizationId, "class:create"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)),
                ClassDTO.class,
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
