package de.adorsys.multibanking.exception.handler;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import de.adorsys.multibanking.exception.base.ErrorKeyException;
import de.adorsys.multibanking.exception.base.ParametrizedMessageException;
import de.adorsys.multibanking.exception.domain.MultibankingError;
import de.adorsys.multibanking.exception.domain.ErrorConstants;


@ControllerAdvice
public class ExceptionHandlerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);
    
    /**
     * Overrides Handle exception of superclass to return a body object if none.
     */
    @Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
			request.setAttribute("javax.servlet.error.exception", ex, 0);
		}
		handleLogging(ex);
		if(body==null)body = MultibankingError.builder().status(status).exception(ex).build();
		if(headers==null)headers = new HttpHeaders();
		return new ResponseEntity<>(body, headers, status);
	}

    private ResponseEntity<Object> handleInternal(Exception ex, MultibankingError error, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
    	ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);
        if (error == null && responseStatus!=null && responseStatus.value()!=null)
        	error = MultibankingError.builder()
        		.status(responseStatus.value())
        		.text(responseStatus.reason()).exception(ex).build();
        if (error == null)
        	error = MultibankingError.builder().status(status).exception(ex).build();
	
        return handleExceptionInternal(ex, error, headers, status, request);
    }
    
    /**
     * Handler for ErrorKeyException
     * which is a custom Exception to deliberately send a http status and an
     * error message key to the frontend.
     * 
     * @param ex ErrorKeyException
     * @return the ApiError object
     */
    @ExceptionHandler
    @ResponseBody
    protected ResponseEntity<Object> handleErrorMessageException(ErrorKeyException ex, WebRequest request) {
        MultibankingError error = MultibankingError.builder().status(ex.getHttpStatus()).exception(ex).text(ex.getErrorKey()).moreInfo(ex.getMoreInfo()).build();
        return handleExceptionInternal(ex, error, null, error.getStatus(), request);
    }
    
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Object> handleException(CompletionException e, WebRequest request) {
        Throwable cause = e.getCause();
        if (RestClientResponseException.class.isAssignableFrom(cause.getClass())) {
            return handleRestClientResponseException((RestClientResponseException) cause, request);
        } else if (ParametrizedMessageException.class.isAssignableFrom(cause.getClass())) {
            return handleException((ParametrizedMessageException) cause, request);
        }
        return handleInternal(e, null, null, null, null);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            LOG.info("User [{}] access denied to [{}] [{}]", httpServletRequest.getRemoteUser(),
                httpServletRequest.getMethod(), httpServletRequest.getRequestURI());
        } catch (Exception ex) {
            LOG.info("Can't LOG: ", ex.getMessage());
        }
        MultibankingError error = MultibankingError.builder().status(HttpStatus.UNAUTHORIZED).text(ErrorConstants.ERR_ACCESS_DENIED).exception(e).build();
        return handleExceptionInternal(e, error, null, error.getStatus(), request);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Object> handleException(ParametrizedMessageException e, WebRequest request) {
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class);
        MultibankingError error = null;
        if (responseStatus != null) {
        	error = MultibankingError.builder().status(responseStatus.value()).text(responseStatus.reason()).params(e.getParamsMap()).exception(e).build();
        } else {
        	error = MultibankingError.builder().status(HttpStatus.BAD_REQUEST).text(e.getMessage()).params(e.getParamsMap()).exception(e).build();
        }
    	return handleExceptionInternal(e, error, null, error.getStatus(), request);
    }

    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Object> handleRestClientResponseException(RestClientResponseException e, WebRequest request) {
    	return handleExceptionInternal(e, e.getResponseBodyAsByteArray(), e.getResponseHeaders(), HttpStatus.valueOf(e.getRawStatusCode()), request);
    }
    
    @ExceptionHandler
    @ResponseBody
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
    	MultibankingError error = MultibankingError.builder().exception(ex).build();
    	error.addValidationErrors(ex.getConstraintViolations());
    	return handleExceptionInternal(ex, error, null, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
    	MultibankingError error = MultibankingError.builder().status(HttpStatus.BAD_REQUEST)
    			.text(ErrorConstants.ERR_PARAMETER)
    			.renderedMessage(ErrorConstants.ERR_PARAMETER_MESSAGE)
    			.exception(ex).build();
        error.getParams().put(ErrorConstants.ERR_PARAMETER_NAME_KEY, ex.getParameterName());
        error.getParams().put(ErrorConstants.ERR_PARAMETER_TYPE_KEY, ex.getParameterType());
        return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        StringBuilder supported = new StringBuilder();
        ex.getSupportedMediaTypes().forEach(t -> supported.append(t).append(" "));
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ErrorConstants.ERR_UNSUPPORTED_MEDIA_TYPE_SUPPORTED_KEY, supported.toString());
        paramMap.put(ErrorConstants.ERR_UNSUPPORTED_MEDIA_TYPE_ACTUAL_KEY, ex.getContentType().toString());

        MultibankingError error = MultibankingError.builder().status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    			.text(ErrorConstants.ERR_UNSUPPORTED_MEDIA_TYPE)
    			.renderedMessage(ErrorConstants.ERR_UNSUPPORTED_MEDIA_TYPE_MESSAGE)
    			.params(paramMap)
    			.exception(ex).build();
        return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }
    
    @Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
    	MultibankingError error = MultibankingError.builder().status(HttpStatus.BAD_REQUEST).exception(ex).build();
    	if(ex.getBindingResult().hasFieldErrors())
    		error.addValidationErrors(ex.getBindingResult().getFieldErrors());
        if (ex.getBindingResult().hasGlobalErrors())
        	error.addGlobalErrors(ex.getBindingResult().getGlobalErrors());
    	return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }

    @Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
			HttpStatus status, WebRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(ErrorConstants.ERR_PARAMETER_VALUE_KEY, "" + ex.getValue());
        paramMap.put(ErrorConstants.ERR_PARAMETER_TYPE_KEY, ex.getRequiredType().getSimpleName());
        if(ex instanceof MethodArgumentTypeMismatchException)
        	paramMap.put(ErrorConstants.ERR_PARAMETER_NAME_KEY, ((MethodArgumentTypeMismatchException)ex).getName());

        MultibankingError error = MultibankingError.builder().status(HttpStatus.BAD_REQUEST)
    			.text(ErrorConstants.ERR_METHOD_ARGUMENT_TYPE_MISMATCH)
    			.renderedMessage(ErrorConstants.ERR_METHOD_ARGUMENT_TYPE_MISMATCH_MESSAGE)
    			.params(paramMap)
    			.exception(ex).build();
        return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }

    @Override
	protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
			WebRequest request) {
    	MultibankingError error = MultibankingError.builder().status(HttpStatus.BAD_REQUEST).exception(ex).build();
    	if(ex.getBindingResult().hasFieldErrors())
    		error.addValidationErrors(ex.getBindingResult().getFieldErrors());
        if (ex.getBindingResult().hasGlobalErrors())
        	error.addGlobalErrors(ex.getBindingResult().getGlobalErrors());
    	return handleExceptionInternal(ex, error, headers, error.getStatus(), request);
    }

    private void handleLogging(Exception ex) {
        handleLogging(ex, false);
    }

    private void handleLogging(Exception ex, boolean skipStacktrace) {
    	LOG.warn("Exception handled by "+ExceptionHandlerAdvice.class.getName()+": " + ex.getClass().getName() + " : " + ex.getLocalizedMessage());
        if (LOG.isDebugEnabled() && !skipStacktrace)
        	LOG.debug("Exception details: ", ex);
    }
}
