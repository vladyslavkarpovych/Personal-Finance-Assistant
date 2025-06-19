package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.Account;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    // Accounts where the user is the owner
    List<Account> findByUserId(Long userId);

    // Accounts where the user is in the sharedUsers collection
    List<Account> findBySharedUsersId(Long userId);
}
