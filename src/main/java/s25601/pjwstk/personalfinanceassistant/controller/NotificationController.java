package s25601.pjwstk.personalfinanceassistant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import s25601.pjwstk.personalfinanceassistant.model.Notification;
import s25601.pjwstk.personalfinanceassistant.repository.NotificationRepository;

@Controller
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/notifications/acknowledge/{id}")
    public String acknowledgeNotification(@PathVariable Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        return "redirect:/profile";
    }
}