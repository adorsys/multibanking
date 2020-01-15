package de.adorsys.multibanking.exception;

import de.adorsys.multibanking.domain.exception.MultibankingException;
import de.adorsys.multibanking.exception.domain.Message;
import de.adorsys.multibanking.exception.domain.Messages;
import de.adorsys.multibanking.logging.RestControllerAspectLogging;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.iban4j.InvalidCheckDigitException;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static de.adorsys.multibanking.exception.domain.Message.Severity.ERROR;
import static de.adorsys.multibanking.logging.RestControllerAspectLogging.AUDIT_LOG;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.util.StringUtils.hasText;

@RequiredArgsConstructor
@Slf4j
@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String INVALD_FORMAT = "INVALID_FORMAT";
    private static final String LOG_FORMAT = "Error: [{}] from Controller: [{}]";

    @ResponseStatus
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(Exception e, HandlerMethod handlerMethod) {
        return handleInternal(e, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(HBCI_Exception e, HandlerMethod handlerMethod) {
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

        return handleInternal(e, Messages.builder().messages(messages).build(), BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(CompletionException e, HandlerMethod handlerMethod) {
        Throwable cause = e.getCause();

        if (HttpStatusCodeException.class.isAssignableFrom(cause.getClass())) {
            return handleHttpStatusCodeException((HttpStatusCodeException) cause, handlerMethod);
        } else if (ParametrizedMessageException.class.isAssignableFrom(cause.getClass())) {
            return handleException((ParametrizedMessageException) cause, handlerMethod);
        }

        return handleInternal(e, handlerMethod);
    }

    @ResponseStatus
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(MultibankingException e, HandlerMethod handlerMethod) {
        HttpStatus httpStatus = valueOf(e.getHttpResponseCode());

        List<Message> messages = e.getMessages().stream()
            .map(message -> Message.builder()
                .key(message.getKey() != null ? message.getKey() : e.getMultibankingError().toString())
                .severity(message.getSeverity() != null
                    ? Message.Severity.valueOf(message.getSeverity().toString())
                    : null)
                .field(message.getField())
                .renderedMessage(message.getRenderedMessage())
                .paramsMap(message.getParamsMap())
                .build()
            ).collect(toList());

        Messages messagesContainer = Messages.builder()
            .messages(messages)
            .build();

        return handleInternal(e, messagesContainer, httpStatus, handlerMethod);
    }

    @ResponseStatus(code = FORBIDDEN)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleAccessDeniedException(ServletRequest request, AccessDeniedException e,
                                                                HandlerMethod handlerMethod) {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            log.info("User [{}] access denied to [{}] [{}]", httpServletRequest.getRemoteUser(),
                httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        } catch (Exception ex) {
            log.info("Can't LOG: {}", ex.getMessage());
        }

        return new ResponseEntity<>(FORBIDDEN);
    }

    @ResponseStatus
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleException(ParametrizedMessageException e, HandlerMethod handlerMethod) {

        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            Messages messages = Messages.createError(responseStatus.reason(), e.getLocalizedMessage(),
                e.getParamsMap());
            return handleInternal(e, messages, responseStatus.code(), handlerMethod);
        } else {
            return handleException(e, handlerMethod);
        }
    }

    @ResponseStatus
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpStatusCodeException(HttpStatusCodeException e,
                                                                  HandlerMethod handlerMethod) {
        return handleInternal(e, null, e.getStatusCode(), handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e,
                                                                              HandlerMethod handlerMethod) {
        Message message = Message.builder()
            .key(INVALD_FORMAT)
            .severity(Message.Severity.ERROR)
            .field(e.getName())
            .renderedMessage(e.getMessage()).build();

        return handleInternal(e, Messages.builder().messages(Collections.singletonList(message)).build(),
            BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex,
                                                                          HandlerMethod handlerMethod) {
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

        return handleInternal(ex, Messages.builder().messages(messages).build(), BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleMethodBindException(BindException ex, HandlerMethod handlerMethod) {
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

        return handleInternal(ex, Messages.builder().messages(messages).build(), BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleConstraintViolationException(ConstraintViolationException e,
                                                                       HandlerMethod handlerMethod) {
        Collection<Message> messages = e.getConstraintViolations().stream()
            .map(cv -> Message.builder()
                .key(VALIDATION_ERROR)
                .severity(Message.Severity.ERROR)
                .field(cv.getPropertyPath().toString())
                .renderedMessage(cv.getMessage())
                .build())
            .collect(toList());

        return handleInternal(e, Messages.builder().messages(messages).build(), BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleUnsatisfiedServletRequestParameterException(ServletRequestBindingException ex, HandlerMethod handlerMethod) {
        return handleInternal(ex, null, BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                          HandlerMethod handlerMethod) {
        return handleInternal(ex, null, BAD_REQUEST, handlerMethod);
    }

    @ResponseStatus(code = METHOD_NOT_ALLOWED)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HandlerMethod handlerMethod) {
        return handleInternal(ex, null, METHOD_NOT_ALLOWED, handlerMethod);
    }

    @ResponseStatus(code = BAD_REQUEST)
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Messages> handleInvalidCheckDigitException(InvalidCheckDigitException ex,
                                                                     HandlerMethod handlerMethod) {
        return handleInternal(ex, Messages.builder()
            .message(Message.builder()
                .key("INVALID_IBAN")
                .field("iban")
                .severity(ERROR)
                .build())
            .build(), BAD_REQUEST, handlerMethod);
    }

    private ResponseEntity<Messages> handleInternal(Throwable throwable, HandlerMethod handlerMethod) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(throwable.getClass(), ResponseStatus.class);

        Messages messages;
        if (responseStatus != null && hasText(responseStatus.reason())) {
            messages = Messages.createError(responseStatus.reason(), throwable.getMessage());
        } else {
            messages = Messages.createError(throwable.getClass().toString(), throwable.getMessage());
        }

        HttpStatus statusCode = Optional.ofNullable(responseStatus)
            .map(ResponseStatus::code)
            .orElse(INTERNAL_SERVER_ERROR);

        return handleInternal(throwable, messages, statusCode, handlerMethod);
    }

    private ResponseEntity<Messages> handleInternal(Throwable throwable, Messages messages, HttpStatus httpStatus,
                                                    HandlerMethod handlerMethod) {
        String controller = handlerMethod.getMethod().getDeclaringClass().getSimpleName();
        String message = throwable instanceof MultibankingException
            ? ((MultibankingException) throwable).getMultibankingError().toString()
            : throwable.getMessage();

        if (httpStatus == NOT_FOUND) {
            log.info(LOG_FORMAT, message, controller);
        } else if (httpStatus.is4xxClientError()) {
            log.warn(LOG_FORMAT, message, controller);
            log.warn(throwable.getMessage(), throwable);
        } else {
            log.error(LOG_FORMAT, message, controller);
            log.error(throwable.getMessage(), throwable);
        }

        ResponseEntity<Messages> responseEntity;
        if (messages == null) {
            responseEntity = new ResponseEntity<>(httpStatus);
        } else {
            responseEntity = new ResponseEntity<>(messages, httpStatus);
        }

        LoggerFactory.getLogger(RestControllerAspectLogging.class).trace(AUDIT_LOG, "Response: [{}]", responseEntity);
        return responseEntity;
    }

}
