package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.session.Session;
import com.houndjo.domain.models.user.User;
import com.houndjo.domain.ports.out.persistenceport.UserPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.SessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps {@link Session} to {@link SessionDTO}, enriching it with the assigned teacher's name when
 * present.
 */
@Component
@RequiredArgsConstructor
public class SessionDtoMapper {

    private final UserPersistencePort userPersistencePort;

    public SessionDTO toDTO(Session session) {
        String teacherName = session.getTeacherUserId() == null ? null : teacherName(session.getTeacherUserId());
        return new SessionDTO(
                session.getId(),
                session.getCourseId(),
                session.getTeacherUserId(),
                teacherName,
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getStatus());
    }

    private String teacherName(Long teacherUserId) {
        return userPersistencePort
                .findWithAuthoritiesById(teacherUserId)
                .map(User::getUserInfo)
                .map(userInfo -> userInfo.firstName() + " " + userInfo.lastName())
                .orElse(null);
    }
}
