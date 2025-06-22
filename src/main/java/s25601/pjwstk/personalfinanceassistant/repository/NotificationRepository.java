package s25601.pjwstk.personalfinanceassistant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import s25601.pjwstk.personalfinanceassistant.model.Notification;
import s25601.pjwstk.personalfinanceassistant.model.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserAndReadFalseOrderByCreatedAtDesc(User user);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.read = false AND " +
            "n.message LIKE %:category% AND n.message LIKE %:period%")
    List<Notification> findUnreadByUserAndCategoryAndPeriod(@Param("user") User user,
                                                            @Param("category") String category,
                                                            @Param("period") String period);
}