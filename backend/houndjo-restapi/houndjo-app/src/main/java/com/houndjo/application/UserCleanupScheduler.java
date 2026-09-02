package com.houndjo.application;

import com.houndjo.domain.ports.in.AccountUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled job that periodically removes stale user accounts (not activated and soft-deleted).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final AccountUseCase accountUseCase;

    /**
     * Not activated and deactivated users should be automatically deleted.
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "${app.account.cleanup-cron}")
    public void removeNotActivatedUsers() {
        log.debug("Scheduled cleanup: removing not activated users");
        accountUseCase.removeNotActivatedUsers();
        log.debug("Scheduled cleanup: removing soft-deleted users");
        accountUseCase.removeSoftDeletedUsers();
    }
}
