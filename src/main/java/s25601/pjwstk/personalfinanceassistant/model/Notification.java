package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notification extends UserOwnedEntity {

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now(); // Association with Attribute

    private boolean read = false; // Association with Attribute
}