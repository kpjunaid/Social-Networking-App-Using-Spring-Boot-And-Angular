package com.kpjunaid.common;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.kpjunaid.error.ValidationError;
import com.kpjunaid.exception.*;
import com.kpjunaid.response.ErrorResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.*;

@RestControllerAdvice
public class CustomExceptionHandler {
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status,
                                                             String message,
                                                             Map<String, List<ValidationError>> validationErrors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .statusCode(status.value())
                .message(message)
                .reason(status.getReasonPhrase())
                .validationErrors(validationErrors)
                .timestamp(new Date())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationError(MethodArgumentNotValidException e) {
        Map<String, List<ValidationError>> validationErrors = new HashMap<>();
        ResponseEntity<ErrorResponse> errorResponseResponseEntity =
                buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", validationErrors);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        for (FieldError fieldError: fieldErrors) {
            List<ValidationError> validationErrorList = Objects.requireNonNull(errorResponseResponseEntity.getBody())
                    .getValidationErrors().get(fieldError.getField());
            if (validationErrorList == null) {
                validationErrorList = new ArrayList<>();
                errorResponseResponseEntity.getBody().getValidationErrors().put(fieldError.getField(), validationErrorList);
            }
            ValidationError validationError = ValidationError.builder()
                    .code(fieldError.getCode())
                    .message(fieldError.getDefaultMessage())
                    .build();
            validationErrorList.add(validationError);
        }

        return errorResponseResponseEntity;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalServerError(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, AppConstants.INTERNAL_SERVER_ERROR, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, AppConstants.ACCESS_DENIED, null);
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(InvalidOperationException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.INVALID_OPERATION, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectCredentialsError(BadCredentialsException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.INCORRECT_CREDENTIALS, null);
    }

    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundError(NoResultException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, AppConstants.NOT_FOUND_ERROR, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMethodError(HttpRequestMethodNotSupportedException e) {
        HttpMethod supportedMethod = Objects.requireNonNull(e.getSupportedHttpMethods()).iterator().next();
        return buildErrorResponse(HttpStatus.METHOD_NOT_ALLOWED,
                String.format(AppConstants.METHOD_NOT_ALLOWED, supportedMethod),
                null);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, AppConstants.FILE_PROCESSING_ERROR, null);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ErrorResponse> handleLockedException(LockedException e) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, AppConstants.ACCOUNT_LOCKED, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleAccountDisabledException(DisabledException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.ACCOUNT_DISABLED, null);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, AppConstants.INVALID_TOKEN, null);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(SignatureVerificationException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, AppConstants.INVALID_TOKEN, null);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(AuthenticationServiceException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.INCORRECT_CREDENTIALS, null);
    }

    @ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExistsException(EmailExistsException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.EMAIL_EXISTS, null);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotFoundException(EmailNotFoundException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.EMAIL_NOT_FOUND, null);
    }

    @ExceptionHandler(SameEmailUpdateException.class)
    public ResponseEntity<ErrorResponse> handleSameEmailUpdateException(SameEmailUpdateException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.SAME_EMAIL, null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, AppConstants.USER_NOT_FOUND, null);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFoundException(PostNotFoundException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.POST_NOT_FOUND, null);
    }

    @ExceptionHandler(TagNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTagNotFoundException(TagNotFoundException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.TAG_NOT_FOUND, null);
    }

    @ExceptionHandler(TagExistsException.class)
    public ResponseEntity<ErrorResponse> handleTagExistsException(TagExistsException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.TAG_EXISTS, null);
    }

    @ExceptionHandler(CountryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCountryNotFoundException(CountryNotFoundException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.COUNTRY_NOT_FOUND, null);
    }

    @ExceptionHandler(CountryExistsException.class)
    public ResponseEntity<ErrorResponse> handleCountryExistsException(CountryExistsException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.COUNTRY_EXISTS, null);
    }

    @ExceptionHandler(EmptyPostException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPostException(EmptyPostException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.EMPTY_POST, null);
    }

    @ExceptionHandler(ShareNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPostException(ShareNotFoundException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.SHARE_NOT_FOUND, null);
    }

    @ExceptionHandler(ShareExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPostException(ShareExistsException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.SHARE_EXISTS, null);
    }

    @ExceptionHandler(DuplicateShareException.class)
    public ResponseEntity<ErrorResponse> handleEmptyPostException(DuplicateShareException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.DUPLICATE_SHARE, null);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(NotificationNotFoundException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, AppConstants.NOTIFICATION_NOT_FOUND, null);
    }
}
