package s25601.pjwstk.personalfinanceassistant.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public String handleUserNotAuthenticated() {
        return "redirect:/login";
    }
}
