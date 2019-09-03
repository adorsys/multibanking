package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.exception.domain.Message;
import de.adorsys.multibanking.exception.domain.Messages;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.InvalidCheckDigitException;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.exception.domain.Message.Severity.ERROR;
import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.hasText;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INVALD_FORMAT = "INVALID_FORMAT";

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(Exception e) {
        return handleInternal(e);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(HBCI_Exception e) {
        Throwable e2 = e;
        List<Message> messages = new ArrayList<>();

        while (e2 != null) {
            if (e2.getMessage() != null) {
                messages.add(Message.builder()
                    .key("HBCI_ERROR")
                    .severity(ERROR)
                    .renderedMessage(e2.getMessage())
                    .build());
            }
            e2 = e2.getCause();
        }

        return handleInternal(e, Messages.builder().messages(messages).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(CompletionException e) {
        Throwable cause = e.getCause();

        if (HttpStatusCodeException.class.isAssignableFrom(cause.getClass())) {
            return handleHttpStatusCodeException((HttpStatusCodeException) cause);
        } else if (ParametrizedMessageException.class.isAssignableFrom(cause.getClass())) {
            return handleException((ParametrizedMessageException) cause);
        }

        return handleInternal(e);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(MultibankingException e) {
        HttpStatus httpStatus = HttpStatus.valueOf(e.getHttpResponseCode());

        Messages messages = Messages.builder()
            .message(Message.builder()
                .key(e.getMultibankingError().toString())
                .renderedMessage(e.getMessage())
                .build())
            .build();

        return handleInternal(e, messages, httpStatus);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleAccessDeniedException(ServletRequest request, AccessDeniedException e) {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            log.info("User [{}] access denied to [{}] [{}]", httpServletRequest.getRemoteUser(),
                httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        } catch (Exception ex) {
            log.info("Can't LOG: {}", ex.getMessage());
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(ParametrizedMessageException e) {

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            Messages messages = Messages.createError(responseStatus.reason(), e.getLocalizedMessage(),
                e.getParamsMap());
            return handleInternal(e, messages, responseStatus.code());
        } else {
            return handleException(e);
        }
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpStatusCodeException(HttpStatusCodeException e) {
        return handleInternal(e, null, e.getStatusCode());
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        Message message = Message.builder()
            .key(INVALD_FORMAT)
            .severity(Message.Severity.ERROR)
            .field(e.getName())
            .renderedMessage(e.getMessage()).build();

        return handleInternal(e, Messages.builder().messages(Collections.singletonList(message)).build(),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Collection<Message> messages = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .field(fieldError.getField())
                .renderedMessage(fieldError.getDefaultMessage())
                .build()).collect(Collectors.toList());

        if (ex.getBindingResult().hasGlobalErrors()) {
            ObjectError objectError = ex.getBindingResult()
                .getGlobalErrors().iterator().next();

            Message message = Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .renderedMessage(objectError.getDefaultMessage())
                .build();

            messages.add(message);
        }

        return handleInternal(
            ex,
            Messages.builder().messages(messages).build(),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodBindException(BindException ex) {
        Collection<Message> messages = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .field(fieldError.getField())
                .renderedMessage(fieldError.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        if (ex.getBindingResult().hasGlobalErrors()) {
            ObjectError objectError = ex.getBindingResult()
                .getGlobalErrors().iterator().next();

            messages.add(Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .renderedMessage(objectError.getDefaultMessage())
                .build()
            );
        }

        return handleInternal(
            ex,
            Messages.builder().messages(messages).build(),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleConstraintViolationException(ConstraintViolationException e) {
        Collection<Message> messages = e.getConstraintViolations().stream()
            .map(cv -> Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .field(cv.getPropertyPath().toString())
                .renderedMessage(cv.getMessage())
                .build())
            .collect(toList());

        return handleInternal(e, Messages.builder().messages(messages).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleUnsatisfiedServletRequestParameterException(ServletRequestBindingException ex) {
        return handleInternal(ex, null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return handleInternal(ex, null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return handleInternal(ex, null, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleInvalidCheckDigitException(InvalidCheckDigitException ex) {
        return handleInternal(ex, Messages.builder()
            .message(Message.builder()
                .key("INVALID_IBAN")
                .field("iban")
                .severity(ERROR)
                .build())
            .build(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Messages> handleInternal(Throwable throwable) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(throwable.getClass(), ResponseStatus.class);

        Messages messages;
        if (responseStatus != null && hasText(responseStatus.reason())) {
            messages = Messages.createError(responseStatus.reason(), throwable.getMessage());
        } else {
            messages = Messages.createError(throwable.getClass().toString(), throwable.getMessage());
        }

        HttpStatus statusCode = Optional.ofNullable(responseStatus)
            .map(ResponseStatus::code)
            .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return handleInternal(throwable, messages, statusCode);
    }

    private ResponseEntity<Messages> handleInternal(Throwable throwable, Messages messages, HttpStatus httpStatus) {
        if (httpStatus == HttpStatus.NOT_FOUND) {
            log.info("Exception {} from Controller: {}", throwable.getClass(), throwable.getMessage());
        } else if (httpStatus.is4xxClientError()) {
            log.warn("Exception {} from Controller: {}", throwable.getClass(),
                NestedExceptionUtils.buildMessage(throwable.getMessage(), throwable.getCause()));
        } else {
            log.error("Exception {} from Controller: {}", throwable.getClass(), throwable);
        }

        if (messages == null) {
            return new ResponseEntity<>(httpStatus);
        } else {
            return new ResponseEntity<>(messages, httpStatus);
        }
    }

}
