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
public class Cashflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nullable
    private String description; // Optional Attribute

    @Enumerated(EnumType.STRING)
    private CashflowType type; // INCOME or EXPENSE

    private BigDecimal amount;

    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Separate categories for income and expense

    @Enumerated(EnumType.STRING)
    private IncomeCategory incomeCategory;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory expenseCategory;
}