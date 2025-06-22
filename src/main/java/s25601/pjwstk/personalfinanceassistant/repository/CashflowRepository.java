package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.Account;
import s25601.pjwstk.personalfinanceassistant.model.Cashflow;

import java.util.List;

public interface CashflowRepository extends JpaRepository<Cashflow, Long> {
    List<Cashflow> findByUserId(Long userId);
    List<Cashflow> findByAccountIn(List<Account> accounts);
}
