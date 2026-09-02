package com.houndjo.infrastructure.adapter.out.persistence;

import com.houndjo.domain.exceptions.RoleGroupNotFoundException;
import com.houndjo.domain.exceptions.UserNotFoundException;
import com.houndjo.domain.models.query.PagedResult;
import com.houndjo.domain.models.rbac.RoleGroup;
import com.houndjo.domain.ports.out.persistenceport.RoleGroupPersistencePort;
import com.houndjo.infrastructure.adapter.out.persistence.entity.RoleGroupEntity;
import com.houndjo.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.houndjo.infrastructure.adapter.out.persistence.mapper.RoleGroupMapper;
import com.houndjo.infrastructure.adapter.out.persistence.repository.RoleGroupRepository;
import com.houndjo.infrastructure.adapter.out.persistence.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing {@link RoleGroupPersistencePort}.
 */
@Service
@RequiredArgsConstructor
public class RoleGroupPersistenceAdapter implements RoleGroupPersistencePort {

    private final RoleGroupRepository roleGroupRepository;
    private final RoleGroupMapper roleGroupMapper;
    private final UserRepository userRepository;

    @Override
    public List<RoleGroup> findAll() {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupMapper.toDomain(roleGroupRepository.findAll()), "Error fetching all role groups");
    }

    @Override
    public PagedResult<RoleGroup> findAll(int page, int size) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    Page<RoleGroupEntity> entityPage = roleGroupRepository.findAll(
                            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creationDate")));
                    List<RoleGroup> items = roleGroupMapper.toDomain(entityPage.getContent());
                    return new PagedResult<>(
                            items, entityPage.getTotalElements(), page, size, entityPage.getTotalPages());
                },
                "Error fetching paginated role groups");
    }

    @Override
    public Optional<RoleGroup> findById(Long id) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupRepository.findById(id).map(roleGroupMapper::toDomain),
                "Error fetching role group by id");
    }

    @Override
    public Set<RoleGroup> findByNames(Collection<String> names) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupMapper.toDomain(roleGroupRepository.findByNameIn(names)),
                "Error fetching role groups by names");
    }

    @Override
    public boolean existsByName(String name) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupRepository.existsByName(name), "Error checking role group name existence");
    }

    @Override
    @Transactional
    public RoleGroup save(RoleGroup roleGroup) {
        return AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupMapper.toDomain(roleGroupRepository.save(roleGroupMapper.toEntity(roleGroup))),
                "Error saving role group");
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> roleGroupRepository.deleteById(id), "Error deleting role group");
    }

    @Override
    @Transactional
    public void assignToUser(Long userId, Long roleGroupId) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    UserEntity user =
                            userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
                    RoleGroupEntity roleGroup = roleGroupRepository
                            .findById(roleGroupId)
                            .orElseThrow(() -> new RoleGroupNotFoundException(roleGroupId));
                    user.getRoleGroups().add(roleGroup);
                    userRepository.save(user);
                },
                "Error assigning role group to user");
    }

    @Override
    @Transactional
    public void revokeFromUser(Long userId, Long roleGroupId) {
        AdapterPersistenceUtils.executeDbOperation(
                () -> {
                    UserEntity user =
                            userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
                    RoleGroupEntity roleGroup = roleGroupRepository
                            .findById(roleGroupId)
                            .orElseThrow(() -> new RoleGroupNotFoundException(roleGroupId));
                    user.getRoleGroups().remove(roleGroup);
                    userRepository.save(user);
                },
                "Error revoking role group from user");
    }
}
