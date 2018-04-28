package exception;

public class ValidationException extends RuntimeException {
	private static final long serialVersionUID = -4776870901880281649L;

	public ValidationException(String message) {
		super(message);
	}

}
