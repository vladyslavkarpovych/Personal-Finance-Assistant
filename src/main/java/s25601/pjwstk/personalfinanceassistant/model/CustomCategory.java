package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "custom_categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name", "type"}))
@Getter
@Setter
public class CustomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;  // e.g. "Gifts"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CashflowType type;  // INCOME or EXPENSE

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}