package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Cashflow extends UserOwnedEntity  {

    @Nullable
    private String description; // Optional Attribute

    @Enumerated(EnumType.STRING)
    private CashflowType type; // INCOME or EXPENSE

    private BigDecimal amount;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account; // Complex Attribute

    @Enumerated(EnumType.STRING)
    private IncomeCategory incomeCategory; // XOR Constraint

    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory; // XOR Constraint
}