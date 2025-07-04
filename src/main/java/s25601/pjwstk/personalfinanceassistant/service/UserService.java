package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.exception.UserNotAuthenticatedException;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return the currently authenticated {@link User} if available, otherwise {@code null}.
     */
    public User getCurrentUser() {
        // Get the current authentication object from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Check if the authentication object is not null and the user is authenticated
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            // Retrieve the identity of the currently authenticated user and check if the principal is an instance of UserDetails
            if (principal instanceof UserDetails userDetails) {
                // Fetch the user from the database using the username from UserDetails
                Optional<User> userOpt = userRepository.findByUsername(userDetails.getUsername());
                return userOpt.orElse(null);
            }
        }
        return null;
    }

    public User getAuthenticatedUser() {
        User user = getCurrentUser();
        if (user == null) {
            throw new UserNotAuthenticatedException();
        }
        return user;
    }
}
