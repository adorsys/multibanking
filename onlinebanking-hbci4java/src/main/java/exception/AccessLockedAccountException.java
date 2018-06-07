package exception;

public class AccessLockedAccountException extends RuntimeException {
    public AccessLockedAccountException() {
        super("Zugang gesperrt, Freischaltung mit TAN erforderlich.");
    }
}
