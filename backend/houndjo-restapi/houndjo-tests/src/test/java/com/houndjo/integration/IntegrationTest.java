package com.houndjo.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houndjo.domain.constants.DomainConstants;
import com.houndjo.domain.enumerations.UserGender;
import com.houndjo.domain.enumerations.UserGroupConstants;
import com.houndjo.domain.enumerations.UserStatus;
import com.houndjo.domain.models.auth.TwoFactorMethodType;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EmbeddableCredentials;
import com.houndjo.infrastructure.adapter.out.persistence.entity.EmbeddableUserInfo;
import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.SecuritySettingsEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.infrastructure.adapter.out.persistence.repository.RoleGroupRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.SecuritySettingsRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
@Import(TestContext.class)
public class IntegrationTest {
    protected static final String DEFAULT_USER_EMAIL = "test@test.com";
    protected static final String DEFAULT_USER_PASSWORD = "P@assw0rd2024";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleGroupRepository roleGroupRepository;

    @Autowired
    protected SecuritySettingsRepository securitySettingsRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        // Truncate all transient tables in one shot; CASCADE handles FK-dependent tables
        // (app_user_role_group, refresh_token, two_factor_challenge, stamp, transaction).
        jdbcTemplate.execute(
                "TRUNCATE TABLE app_user, user_preference, app_configuration, app_security_settings, organization, membership, school_class, course, student, enrollment, course_pace, session, progress_record, attendance, attendance_permission RESTART IDENTITY CASCADE");
        // role_group holds both seed data (last_updated_by='system') and test-created rows.
        // Delete only the test rows; ON DELETE CASCADE handles role_group_permission automatically.
        jdbcTemplate.execute("DELETE FROM role_group WHERE last_updated_by <> 'system'");
    }

    protected <T> T get(String url, TypeReference<T> responseType, ResultMatcher... matchers) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();
        if (response.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(response, responseType);
    }

    protected <T> T get(String url, Class<T> responseType, ResultMatcher... matchers) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();
        if (response.isEmpty()) {
            return null;
        }

        return objectMapper.readValue(response, responseType);
    }

    protected void get(String url, ResultMatcher... matchers) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(url)).andExpectAll(matchers);
    }

    protected <T> T post(String url, Object requestBody, TypeReference<T> typeReference, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, typeReference);
    }

    protected <T> T post(String url, Object requestBody, Class<T> responseType, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, responseType);
    }

    protected void post(String url, Object requestBody, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    protected <T> T postText(String url, String textContent, Class<T> responseType, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).contentType(TEXT_PLAIN_VALUE);

        if (textContent != null) {
            builder.content(textContent);
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, responseType);
    }

    protected void postText(String url, String textContent, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post(url).contentType(TEXT_PLAIN_VALUE);

        if (textContent != null) {
            builder.content(textContent);
        }

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    protected <T> T put(String url, Object requestBody, TypeReference<T> typeReference, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, typeReference);
    }

    protected <T> T put(String url, Object requestBody, Class<T> responseType, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, responseType);
    }

    protected void put(String url, Object requestBody, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    protected void delete(String url, Object requestBody, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.delete(url).contentType(APPLICATION_JSON_VALUE);
        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }
        mockMvc.perform(builder).andExpectAll(matchers);
    }

    protected <T> T delete(String url, TypeReference<T> typeReference, ResultMatcher... matchers) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, typeReference);
    }

    protected <T> T delete(String url, Class<T> responseType, ResultMatcher... matchers) throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, responseType);
    }

    protected void delete(String url, ResultMatcher... matchers) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpectAll(matchers);
    }

    protected <T> T patch(String url, Object requestBody, TypeReference<T> typeReference, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.patch(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, typeReference);
    }

    protected <T> T patch(String url, Object requestBody, Class<T> responseType, ResultMatcher... matchers)
            throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.patch(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        String response = mockMvc.perform(builder)
                .andExpectAll(matchers)
                .andReturn()
                .getResponse()
                .getContentAsString();

        return response.isEmpty() ? null : objectMapper.readValue(response, responseType);
    }

    protected void patch(String url, Object requestBody, ResultMatcher... matchers) throws Exception {
        MockHttpServletRequestBuilder builder =
                MockMvcRequestBuilders.patch(url).contentType(APPLICATION_JSON_VALUE);

        if (requestBody != null) {
            builder.content(objectMapper.writeValueAsString(requestBody));
        }

        mockMvc.perform(builder).andExpectAll(matchers);
    }

    /**
     * Creates a new activated user with the specified email and role groups.
     *
     * @param email          the email address of the user to be created
     * @param roleGroupNames the set of role group names to assign to the user
     * @return the created and saved user
     */
    protected UserEntity createUser(String email, Set<String> roleGroupNames) {
        Set<RoleGroupEntity> dbRoleGroups = roleGroupNames.stream()
                .map(name -> roleGroupRepository
                        .findByNameIn(Set.of(name))
                        .iterator()
                        .next())
                .collect(Collectors.toSet());

        UserEntity user = new UserEntity(null, null, null, null, new HashSet<>(), false, null, null);
        user.setUserInfo(buildUserInfo());
        user.setUserCredentials(buildCredentials(
                email, passwordEncoder.encode(DEFAULT_USER_PASSWORD), null, Instant.now(), null, null));
        user.setStatus(UserStatus.ACTIVATED);
        user.setRoleGroups(dbRoleGroups);
        user.setCreationDate(Instant.now());
        user.setLastUpdateDate(Instant.now());
        user.setLastUpdatedBy("test");

        return userRepository.save(user);
    }

    /**
     * Creates a minimal user with no role groups assigned, suitable for tests that
     * do not need specific permissions (e.g. authentication flow tests).
     *
     * @param email the user email address
     * @return the created user
     */
    protected UserEntity createUser(String email) {
        return createUser(email, Set.of("User"));
    }

    protected UserEntity createUserWithoutRole(String email) {
        return createUser(email, Set.of());
    }

    protected UserEntity createDefaultUser() {
        return createUser(DEFAULT_USER_EMAIL);
    }

    protected UserEntity createUserWithTwoFactor(String email, TwoFactorMethodType method) {
        UserEntity user = createUser(email);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(method);
        return userRepository.save(user);
    }

    protected UserEntity createUserWithTotp(String email, String totpSecret) {
        UserEntity user = createUser(email);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(TwoFactorMethodType.TOTP);
        user.setTotpSecret(totpSecret);
        return userRepository.save(user);
    }

    /**
     * Creates a new non-activated user with the specified email and role groups.
     *
     * @param email          the email address of the user to be created
     * @param roleGroupNames the set of role group names to assign to the user
     * @return the created and saved user
     */
    protected UserEntity createNonActiveUser(String email, Set<String> roleGroupNames) {
        Set<RoleGroupEntity> dbRoleGroups = roleGroupNames.stream()
                .map(name -> roleGroupRepository
                        .findByNameIn(Set.of(name))
                        .iterator()
                        .next())
                .collect(Collectors.toSet());

        UserEntity user = new UserEntity(null, null, null, null, new HashSet<>(), false, null, null);
        user.setUserInfo(buildUserInfo());
        user.setUserCredentials(
                buildCredentials(email, passwordEncoder.encode(DEFAULT_USER_PASSWORD), "ABDCD00", null, null, null));
        user.setStatus(UserStatus.NOT_ACTIVATED);
        user.setRoleGroups(dbRoleGroups);
        user.setCreationDate(Instant.now());
        user.setLastUpdateDate(Instant.now());
        user.setLastUpdatedBy("test");

        return userRepository.save(user);
    }

    protected UserEntity createNonActiveUser(String email) {
        return createNonActiveUser(email, Set.of(UserGroupConstants.USER));
    }

    protected UserEntity createNonActiveUser(String email, Instant creationDate) {
        Set<RoleGroupEntity> dbRoleGroups = new HashSet<>();

        UserEntity user = new UserEntity(null, null, null, null, new HashSet<>(), false, null, null);

        user.setUserInfo(buildUserInfo());
        user.setUserCredentials(
                buildCredentials(email, passwordEncoder.encode(DEFAULT_USER_PASSWORD), "ABDCD00", null, null, null));
        user.setStatus(UserStatus.NOT_ACTIVATED);
        user.setRoleGroups(dbRoleGroups);
        user.setCreationDate(Instant.now());
        user.setLastUpdateDate(Instant.now());
        user.setLastUpdatedBy("test");

        UserEntity savedUser = userRepository.save(user);
        forceUserCreationDate(savedUser.getId(), creationDate);
        return userRepository.findById(savedUser.getId()).orElseThrow();
    }

    private static EmbeddableUserInfo buildUserInfo() {
        return new EmbeddableUserInfo(
                "Mamadou",
                "Diallo",
                null,
                LocalDate.of(1990, 1, 1),
                UserGender.MALE,
                null,
                DomainConstants.DEFAULT_LANGUAGE,
                null);
    }

    private static EmbeddableCredentials buildCredentials(
            String email,
            String passwordHash,
            String activationCode,
            Instant activationDate,
            String resetCode,
            Instant resetDate) {
        return new EmbeddableCredentials(
                email.toLowerCase(),
                passwordHash,
                activationCode,
                activationDate,
                resetCode,
                resetDate,
                null,
                null,
                null);
    }

    protected void enableGlobalTwoFactorRequirement() {
        SecuritySettingsEntity entity = new SecuritySettingsEntity(null, true);
        entity.setCreationDate(Instant.now());
        entity.setLastUpdateDate(Instant.now());
        entity.setLastUpdatedBy("test");
        securitySettingsRepository.save(entity);
    }

    private void forceUserCreationDate(Long userId, Instant creationDate) {
        int updated = jdbcTemplate.update(
                "UPDATE app_user SET creation_date = ?, last_update_date = ? WHERE id = ?",
                Timestamp.from(creationDate),
                Timestamp.from(creationDate),
                userId);
        if (updated != 1) {
            throw new RuntimeException("Expected 1 row updated, got " + updated);
        }
    }
}
