package de.adorsys.multibanking.exception;

public class InvalidRowException extends RuntimeException {
	private static final long serialVersionUID = 5990327737750485489L;

	public InvalidRowException(String message) {
		super(message);
	}

	public InvalidRowException(String message, Throwable cause) {
		super(message, cause);
	}
}
