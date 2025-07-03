package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Cashflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nullable
    @Length(max = 50, message = "Description cannot exceed 50 characters")
    private String description; // optional attribute

    @Enumerated(EnumType.STRING)
    private CashflowType type; // complex attribute

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amount; // simple attribute, constrained

    @NotNull
    private LocalDate date; // simple attribute, constrained

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // association attribute

    @Enumerated(EnumType.STRING)
    private IncomeCategory incomeCategory;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory;

    @Column(nullable = true)
    private String customIncomeCategoryName;

    @Column(nullable = true)
    private String customExpenseCategoryName;

    @Column(nullable = true)
    private String newCustomIncomeCategoryName;

    @Column(nullable = true)
    private String newCustomExpenseCategoryName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Association to User
}