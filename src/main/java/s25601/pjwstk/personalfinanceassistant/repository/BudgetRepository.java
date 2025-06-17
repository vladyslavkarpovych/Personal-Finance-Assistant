package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.Budget;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.model.ExpenseCategory;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUser(User user);
    Optional<Budget> findByUserAndCategory(User user, ExpenseCategory category);
}