package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("DEBT_REPAYMENT")
@Getter
@Setter
public class DebtRepaymentGoal extends Goal {
    private BigDecimal interestRate;
    private String lenderName;
}
