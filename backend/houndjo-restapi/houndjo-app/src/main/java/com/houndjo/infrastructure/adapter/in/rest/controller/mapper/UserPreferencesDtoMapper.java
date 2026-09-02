package com.houndjo.infrastructure.adapter.in.rest.controller.mapper;

import com.houndjo.domain.models.userpreference.AppearancePreferences;
import com.houndjo.domain.models.userpreference.DisplayPreferences;
import com.houndjo.domain.models.userpreference.NotificationPreferences;
import com.houndjo.domain.models.userpreference.UserPreferences;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.AppearancePreferencesDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.DisplayPreferencesDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.NotificationPreferencesDTO;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.UserPreferencesDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct mapper between preference domain models and REST DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserPreferencesDtoMapper {

    default UserPreferencesDTO toDTO(UserPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        return new UserPreferencesDTO(
                toDTO(preferences.appearance()), toDTO(preferences.notifications()), toDTO(preferences.display()));
    }

    default UserPreferences toDomain(UserPreferencesDTO preferences) {
        if (preferences == null) {
            return null;
        }
        return UserPreferences.of(
                toDomain(preferences.appearance()),
                toDomain(preferences.notifications()),
                toDomain(preferences.display()));
    }

    default AppearancePreferencesDTO toDTO(AppearancePreferences appearance) {
        if (appearance == null) {
            return null;
        }
        return new AppearancePreferencesDTO(appearance.getTheme(), appearance.getFont());
    }

    default AppearancePreferences toDomain(AppearancePreferencesDTO appearance) {
        if (appearance == null) {
            return null;
        }
        return AppearancePreferences.of(appearance.theme(), appearance.font());
    }

    default NotificationPreferencesDTO toDTO(NotificationPreferences notifications) {
        if (notifications == null) {
            return null;
        }
        return new NotificationPreferencesDTO(notifications.productUpdatesEnabled());
    }

    default NotificationPreferences toDomain(NotificationPreferencesDTO notifications) {
        if (notifications == null) {
            return null;
        }
        return new NotificationPreferences(notifications.productUpdatesEnabled());
    }

    default DisplayPreferencesDTO toDTO(DisplayPreferences display) {
        if (display == null) {
            return null;
        }
        return new DisplayPreferencesDTO(display.textSize(), display.reduceMotion());
    }

    default DisplayPreferences toDomain(DisplayPreferencesDTO display) {
        if (display == null) {
            return null;
        }
        return new DisplayPreferences(display.textSize(), display.reduceMotion());
    }
}
