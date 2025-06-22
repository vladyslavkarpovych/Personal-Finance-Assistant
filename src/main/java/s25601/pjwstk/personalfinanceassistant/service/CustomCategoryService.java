package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.model.CashflowType;
import s25601.pjwstk.personalfinanceassistant.model.CustomCategory;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.CustomCategoryRepository;

import java.util.List;

@Service
public class CustomCategoryService {

    private final CustomCategoryRepository customCategoryRepository;

    public CustomCategoryService(CustomCategoryRepository customCategoryRepository) {
        this.customCategoryRepository = customCategoryRepository;
    }

    public List<CustomCategory> getCategoriesByUserAndType(User user, CashflowType type) {
        return customCategoryRepository.findByUserAndType(user, type);
    }

    public boolean categoryExists(User user, String name, CashflowType type) {
        return customCategoryRepository.existsByUserAndNameIgnoreCaseAndType(user, name, type);
    }

    public CustomCategory addCustomCategory(User user, String name, CashflowType type) {
        if (categoryExists(user, name, type)) {
            throw new IllegalArgumentException("Category already exists");
        }
        CustomCategory category = new CustomCategory();
        category.setUser(user);
        category.setName(name);
        category.setType(type);
        return customCategoryRepository.save(category);
    }
}