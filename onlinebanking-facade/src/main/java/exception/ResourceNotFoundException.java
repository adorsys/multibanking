package exception;

public class ResourceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1781257842190565308L;

	public ResourceNotFoundException(String msg) {
        super(msg);
    }

}
