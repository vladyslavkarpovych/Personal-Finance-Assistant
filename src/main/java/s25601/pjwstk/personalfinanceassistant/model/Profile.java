package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // class attribute, primary key

    @NotBlank(message = "Full name is required")
    private String fullName; // simple attribute, mandatory

    @Min(value = 0, message = "Monthly income must be positive") // Custom Constraint
    private double monthlyIncome; // simple attribute, custom constraint

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user; // binary association attribute
}
