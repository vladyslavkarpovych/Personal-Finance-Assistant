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

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
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
