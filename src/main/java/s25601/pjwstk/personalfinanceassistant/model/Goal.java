package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "goal_type")
@Getter
@Setter
public abstract class Goal implements Comparable<Goal> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @NotBlank(message = "Goal name is required")
    private String name; // attribute constraint: must be not blank

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal targetAmount; // attribute constraint:  must be positive and not null

    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Future(message = "Due date must be in the future")
    private LocalDate dueDate; // attribute constraint: must be in the future

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public int compareTo(Goal other) {
        if (this.dueDate == null && other.dueDate == null) {
            return 0;
        }
        if (this.dueDate == null) {
            return 1; // goals with null dueDate go after goals with dueDate
        }
        if (other.dueDate == null) {
            return -1; // other goal with null dueDate go after this goal with dueDate
        }
        return this.dueDate.compareTo(other.dueDate);
    }
}
