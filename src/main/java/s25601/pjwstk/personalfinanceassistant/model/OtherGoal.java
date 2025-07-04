package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("OTHER")
public class OtherGoal extends Goal{
}
