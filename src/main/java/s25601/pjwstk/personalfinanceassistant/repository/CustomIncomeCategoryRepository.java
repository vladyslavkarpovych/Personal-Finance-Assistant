package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.CustomIncomeCategory;

import java.util.List;

public interface CustomIncomeCategoryRepository extends JpaRepository<CustomIncomeCategory, Long> {
    List<CustomIncomeCategory> findByUserId(Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
}
