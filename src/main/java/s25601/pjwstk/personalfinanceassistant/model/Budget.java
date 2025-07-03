package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity // Extent - persistency
@Getter
@Setter
public class Budget extends UserOwnedEntity {

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category; // complex attribute

    private BigDecimal limitAmount; // simple attribute

    @Enumerated(EnumType.STRING)
    private BudgetPeriod period = BudgetPeriod.MONTHLY; // complex attribute

    @Transient
    private BigDecimal remaining; // derived attribute
}
