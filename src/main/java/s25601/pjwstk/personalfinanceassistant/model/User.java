package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public static final int MAX_ACCOUNTS = 3; // class attribute

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username; // simple attribute, mandatory and unique Constraint

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password; // simple attribute, mandatory

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email; // simple attribute, mandatory, unique, with email format validation

    @ManyToMany(mappedBy = "sharedUsers")
    private Set<Account> sharedAccounts = new HashSet<>(); // multi-valued attribute, many-to-many association

    // class method
    public static int getMaxProfiles() { // Class Method
        return MAX_ACCOUNTS;
    }
}
