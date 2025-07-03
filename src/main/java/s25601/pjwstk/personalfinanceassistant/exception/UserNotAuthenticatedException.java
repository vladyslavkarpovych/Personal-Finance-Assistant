package s25601.pjwstk.personalfinanceassistant.exception;

// Inheritance
public class UserNotAuthenticatedException extends RuntimeException {

    // Overrides the constructor
    public UserNotAuthenticatedException() {
        super("User is not authenticated");
    }

}
