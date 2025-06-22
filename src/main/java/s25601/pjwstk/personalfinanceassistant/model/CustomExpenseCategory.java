package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CustomExpenseCategory extends UserOwnedEntity {

    @Column(nullable = false)
    private String name;
}
