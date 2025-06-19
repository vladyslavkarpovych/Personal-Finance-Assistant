package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Budget extends UserOwnedEntity {

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    private BudgetPeriod period = BudgetPeriod.MONTHLY;

    @Transient
    private BigDecimal remaining;
}
