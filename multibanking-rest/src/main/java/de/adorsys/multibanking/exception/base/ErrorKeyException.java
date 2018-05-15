package de.adorsys.multibanking.exception.base;

import org.springframework.http.HttpStatus;

import de.adorsys.multibanking.exception.handler.ExceptionHandlerAdvice;
import lombok.Builder;
import lombok.Data;

/**
 * Custom Exception to be handled by the {@link ExceptionHandlerAdvice} with the
 * {@link HttpStatus}, an error.key and moreInfo text
 *
 * @author fpo 2018-04-07 08:43
 */
@Data
@Builder
public class ErrorKeyException extends RuntimeException {
	private static final long serialVersionUID = -4207421762658448068L;
	private HttpStatus httpStatus;
    private String errorKey;
    private String moreInfo;
}
