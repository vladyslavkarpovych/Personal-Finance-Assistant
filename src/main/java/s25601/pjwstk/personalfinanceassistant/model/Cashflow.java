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
public class Cashflow extends UserOwnedEntity  {

    @Nullable
    @Length(max = 250, message = "Description cannot exceed 250 characters")
    private String description; // Optional Attribute

    @Enumerated(EnumType.STRING)
    private CashflowType type; // INCOME or EXPENSE

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amount;

    @NotNull
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // Complex Attribute

    @Enumerated(EnumType.STRING)
    private IncomeCategory incomeCategory; // XOR Constraint

    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory; // XOR Constraint

    @Column(nullable = true)
    private String customIncomeCategoryName;

    @Column(nullable = true)
    private String customExpenseCategoryName;

    @Column(nullable = true)
    private String newCustomIncomeCategoryName;

    @Column(nullable = true)
    private String newCustomExpenseCategoryName;
}