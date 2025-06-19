package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.model.Budget;
import s25601.pjwstk.personalfinanceassistant.model.Notification;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.NotificationRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyBudgetExceeded(User user, String category, String period, String limit, String spent) {
        List<Notification> existing = notificationRepository.findUnreadByUserAndCategoryAndPeriod(user, category, period); // Qualified Association

        if (existing.isEmpty()) {
            String message = String.format(
                    "Budget exceeded for category %s (%s period). Limit: %s, Spent: %s",
                    category, period, limit, spent);

            Notification notification = new Notification();
            notification.setUser(user); // Binary Association
            notification.setMessage(message);

            notificationRepository.save(notification);
        }
    }

    // Overloaded method - accept Budget object instead of strings
    public void notifyBudgetExceeded(User user, Budget budget, BigDecimal spent) {
        String category = budget.getCategory().name();
        String period = budget.getPeriod().name();
        String limit = budget.getLimitAmount().toString();

        notifyBudgetExceeded(user, category, period, limit, spent.toString());
    }

    // Overloaded method - just user and category with a default message
    public void notifyBudgetExceeded(User user, String category) {
        String message = String.format("Budget exceeded for category %s.", category);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);

        notificationRepository.save(notification);
    }

}