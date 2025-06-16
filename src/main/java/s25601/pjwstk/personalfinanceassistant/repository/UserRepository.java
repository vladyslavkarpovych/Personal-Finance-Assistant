package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
