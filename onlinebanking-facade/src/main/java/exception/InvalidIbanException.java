package exception;

/**
 * @author cga
 */
public class InvalidIbanException extends RuntimeException {
	private static final long serialVersionUID = 4655354574351100460L;

	public InvalidIbanException(String message) {
		super(message);
	}
}
