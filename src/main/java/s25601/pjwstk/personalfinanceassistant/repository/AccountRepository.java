package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.Account;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
}
