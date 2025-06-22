package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.CustomExpenseCategory;

import java.util.List;

public interface CustomExpenseCategoryRepository extends JpaRepository<CustomExpenseCategory, Long> {
    List<CustomExpenseCategory> findByUserId(Long userId);
}
