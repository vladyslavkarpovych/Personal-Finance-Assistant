package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("INVESTMENT")
@Setter
@Getter
public class InvestmentGoal extends Goal {
    private String riskLevel;
    private BigDecimal expectedReturn;
}
