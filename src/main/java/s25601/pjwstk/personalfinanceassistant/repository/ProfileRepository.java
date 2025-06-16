package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
