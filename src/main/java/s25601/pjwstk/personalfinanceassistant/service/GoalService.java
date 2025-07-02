package s25601.pjwstk.personalfinanceassistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import s25601.pjwstk.personalfinanceassistant.model.Goal;
import s25601.pjwstk.personalfinanceassistant.model.User;
import s25601.pjwstk.personalfinanceassistant.repository.GoalRepository;
import java.util.List;

@Service
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Goal> getGoalsForUser(User user) {
        return goalRepository.findByUser(user);
    }

    public void saveGoal(Goal goal) {
        goalRepository.save(goal);
    }

    public void deleteGoal(Long id) {
        goalRepository.deleteById(id);
    }
}
