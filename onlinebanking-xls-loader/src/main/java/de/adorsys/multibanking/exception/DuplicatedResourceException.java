package de.adorsys.multibanking.exception;

public class DuplicatedResourceException extends RuntimeException {
	private static final long serialVersionUID = 3928948758826868879L;

	public DuplicatedResourceException(String message) {
		super(message);
	}
}
