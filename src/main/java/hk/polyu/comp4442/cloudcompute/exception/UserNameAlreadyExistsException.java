package hk.polyu.comp4442.cloudcompute.exception;

// Custom exception: username already taken
public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
