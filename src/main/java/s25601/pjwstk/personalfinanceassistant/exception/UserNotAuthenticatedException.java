package s25601.pjwstk.personalfinanceassistant.exception;

public class UserNotAuthenticatedException extends RuntimeException {

    public UserNotAuthenticatedException() {
        super("User is not authenticated");
    }

}
