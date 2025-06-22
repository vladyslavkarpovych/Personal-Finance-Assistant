package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import s25601.pjwstk.personalfinanceassistant.model.CashflowType;
import s25601.pjwstk.personalfinanceassistant.model.CustomCategory;
import s25601.pjwstk.personalfinanceassistant.model.User;

import java.util.List;

public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {
    List<CustomCategory> findByUserAndType(User user, CashflowType type);
    boolean existsByUserAndNameIgnoreCaseAndType(User user, String name, CashflowType type);
}

