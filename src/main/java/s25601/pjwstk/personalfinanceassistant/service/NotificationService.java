package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.model.Notification;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.NotificationRepository;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void notifyBudgetExceeded(User user, String category, String period, String limit, String spent) {
        List<Notification> existing = notificationRepository.findUnreadByUserAndCategoryAndPeriod(user, category, period);

        if (existing.isEmpty()) {
            String message = String.format(
                    "Budget exceeded for category %s (%s period). Limit: %s, Spent: %s",
                    category, period, limit, spent);

            Notification notification = new Notification();
            notification.setUser(user);
            notification.setMessage(message);

            notificationRepository.save(notification);
        }
    }

}