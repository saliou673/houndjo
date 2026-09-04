package com.houndjo.infrastructure.adapter.in.rest.controller;

import com.houndjo.domain.exceptions.*;
import com.houndjo.infrastructure.adapter.in.rest.controller.dto.ValidationErrorResponseDTO;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Translates domain and validation exceptions into structured, localized HTTP error responses.
 * <p>
 * Domain exceptions ({@link FunctionalException}, {@link TechnicalException}) carry a stable
 * {@link LocalizedError#getCode() message code} and {@link LocalizedError#getArgs() args} rather
 * than a display-ready string, so the domain layer stays free of any i18n framework dependency.
 * This adapter is where the code+args pair is resolved to a locale-specific message, using the
 * incoming request's resolved {@link java.util.Locale} (see {@code LocaleConfiguration}).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleInvalidImage(ResponseStatusException ex) {
        logError(ex);
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token", resolveMessage(ex));
    }

    @ExceptionHandler(TwoFactorSetupRequiredException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ValidationErrorResponseDTO> handleTwoFactorSetupRequired(TwoFactorSetupRequiredException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.FORBIDDEN, "2FA Setup Required", resolveMessage(ex));
    }

    @ExceptionHandler(AuthFunctionalException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleAuthenticationExceptions(AuthFunctionalException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Error", resolveMessage(ex));
    }

    @ExceptionHandler(UserAlreadyActivatedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleUserAlreadyActivated(UserAlreadyActivatedException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "User Error", resolveMessage(ex));
    }

    @ExceptionHandler(FunctionalException.class)
    public ResponseEntity<ValidationErrorResponseDTO> handleUserException(FunctionalException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Business Error", resolveMessage(ex));
    }

    @ExceptionHandler(TechnicalException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ValidationErrorResponseDTO> handleTechnicalException(TechnicalException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Technical Error", resolveMessage(ex));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> IllegalArgumentException(IllegalArgumentException ex) {
        logError(ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logError(ex);

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();

            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponseDTO errorResponse = new ValidationErrorResponseDTO(
                LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Validation Error", errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Resolves a {@link LocalizedError}'s message code + args to the current request locale,
     * falling back to the exception's own (English) message if no translation is found.
     */
    private <T extends RuntimeException & LocalizedError> String resolveMessage(T ex) {
        return messageSource.getMessage(ex.getCode(), ex.getArgs(), ex.getMessage(), LocaleContextHolder.getLocale());
    }

    private ResponseEntity<ValidationErrorResponseDTO> buildErrorResponse(
            HttpStatus status, String title, String message) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", message);

        ValidationErrorResponseDTO errorResponse =
                new ValidationErrorResponseDTO(LocalDateTime.now(), status.value(), title, errors);

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleAlreadyExists(UserAlreadyExistsException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "User Error", resolveMessage(ex));
    }

    @ExceptionHandler(TwoFactorAlreadyEnabledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleTwoFactorAlreadyEnabled(
            TwoFactorAlreadyEnabledException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "2FA Error", resolveMessage(ex));
    }

    @ExceptionHandler(RoleGroupNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleRoleGroupNotFound(RoleGroupNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Role Group Error", resolveMessage(ex));
    }

    @ExceptionHandler(RoleGroupNameAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleRoleGroupNameConflict(
            RoleGroupNameAlreadyExistsException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "Role Group Error", resolveMessage(ex));
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleOrganizationNotFound(OrganizationNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Organization Error", resolveMessage(ex));
    }

    @ExceptionHandler(MembershipNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleMembershipNotFound(MembershipNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Membership Error", resolveMessage(ex));
    }

    @ExceptionHandler(SchoolClassNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleSchoolClassNotFound(SchoolClassNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Class Error", resolveMessage(ex));
    }

    @ExceptionHandler(CourseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleCourseNotFound(CourseNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Course Error", resolveMessage(ex));
    }

    @ExceptionHandler(StudentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleStudentNotFound(StudentNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Student Error", resolveMessage(ex));
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleEnrollmentNotFound(EnrollmentNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Enrollment Error", resolveMessage(ex));
    }

    @ExceptionHandler(DuplicateActiveEnrollmentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleDuplicateActiveEnrollment(
            DuplicateActiveEnrollmentException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "Enrollment Error", resolveMessage(ex));
    }

    @ExceptionHandler(InvitationAlreadyPendingException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ValidationErrorResponseDTO> handleInvitationAlreadyPending(
            InvitationAlreadyPendingException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.CONFLICT, "Invitation Error", resolveMessage(ex));
    }

    @ExceptionHandler(SurahNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleSurahNotFound(SurahNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Quran Reference Error", resolveMessage(ex));
    }

    @ExceptionHandler(VerseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleVerseNotFound(VerseNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Quran Reference Error", resolveMessage(ex));
    }

    @ExceptionHandler(PageNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handlePageNotFound(PageNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Quran Reference Error", resolveMessage(ex));
    }

    @ExceptionHandler(JuzNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ValidationErrorResponseDTO> handleJuzNotFound(JuzNotFoundException ex) {
        logError(ex);
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Quran Reference Error", resolveMessage(ex));
    }

    private static void logError(Exception ex) {
        log.error("Error occurred: {}", ex.getMessage(), ex);
    }
}
