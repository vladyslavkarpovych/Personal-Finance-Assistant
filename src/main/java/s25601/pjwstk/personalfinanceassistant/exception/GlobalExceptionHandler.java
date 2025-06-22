package s25601.pjwstk.personalfinanceassistant.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotAuthenticatedException.class)
    public String handleUserNotAuthenticated() {
        return "redirect:/login";
    }

    @ExceptionHandler(DuplicateCategoryException.class)
    public String handleDuplicateCategoryException(DuplicateCategoryException ex, Model model) {
        model.addAttribute("duplicateCategoryError", ex.getMessage());
        return "cashflow_form";  // Return the same form view
    }
}
