package com.houndjo.infrastructure.adapter.out.query;

import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.models.user.UserFilter;
import com.houndjo.domain.ports.in.UserQueryUseCase;
import com.houndjo.infrastructure.adapter.out.persistence.entity.*;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.UserMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserPreferenceRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link UserQueryUseCase}.
 * Translates domain {@link UserFilter} into JPA {@link Specification} predicates.
 * All filter operators (equals, contains, in, range, etc.) are fully supported.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserQueryService extends QueryService<UserEntity> implements UserQueryUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserPreferenceRepository userPreferenceRepository;

    @Override
    public PagedResult<User> findAll(UserFilter filter, int page, int size) {
        log.debug("Finding users by filter: {}", filter);
        Page<UserEntity> entityPage = userRepository.findAll(
                createSpecification(filter),
                PageRequest.of(page, size, Sort.by("id").ascending()));
        Map<Long, UserPreferenceEntity> preferencesByUserId = userPreferenceRepository
                .findAllByUserIdIn(
                        entityPage.getContent().stream().map(UserEntity::getId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(UserPreferenceEntity::getUserId, Function.identity()));
        List<User> users = entityPage.getContent().stream()
                .map(userMapper::toDomain)
                .peek(user -> {
                    UserPreferenceEntity preference = preferencesByUserId.get(user.getId());
                    if (preference != null) {
                        user.updatePreferences(preference.getPreferences());
                    }
                })
                .toList();
        return new PagedResult<>(users, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
    }

    @Override
    public long count(UserFilter filter) {
        log.debug("Counting users by filter: {}", filter);
        return userRepository.count(createSpecification(filter));
    }

    private Specification<UserEntity> createSpecification(UserFilter filter) {
        Specification<UserEntity> spec = Specification.unrestricted();

        if (filter == null) {
            return spec;
        }

        if (filter.getEmail() != null) {
            spec = spec.and(buildSpecification(
                    filter.getEmail(),
                    root -> root.get(UserEntity_.userCredentials).get(EmbeddableCredentials_.email)));
        }

        if (filter.getFirstName() != null) {
            spec = spec.and(buildSpecification(
                    filter.getFirstName(),
                    root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.firstName)));
        }

        if (filter.getLastName() != null) {
            spec = spec.and(buildSpecification(
                    filter.getLastName(), root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.lastName)));
        }

        if (filter.getGender() != null) {
            spec = spec.and(buildSpecification(
                    filter.getGender(), root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.gender)));
        }

        if (filter.getStatus() != null) {
            spec = spec.and(buildEnumSpecification(filter.getStatus(), UserEntity_.status));
        }

        if (filter.getPhoneNumber() != null) {
            spec = spec.and(buildSpecification(
                    filter.getPhoneNumber(),
                    root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.phoneNumber)));
        }

        if (filter.getAddress() != null) {
            spec = spec.and(buildSpecification(
                    filter.getAddress(), root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.address)));
        }

        if (filter.getLanguageKey() != null) {
            spec = spec.and(buildSpecification(
                    filter.getLanguageKey(),
                    root -> root.get(UserEntity_.userInfo).get(EmbeddableUserInfo_.languageKey)));
        }

        // Add audit fields specifications
        spec = addAuditFieldsSpecifications(
                spec, filter, UserEntity_.creationDate, UserEntity_.lastUpdateDate, UserEntity_.lastUpdatedBy);

        return spec;
    }
}
