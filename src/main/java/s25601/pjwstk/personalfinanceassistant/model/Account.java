package s25601.pjwstk.personalfinanceassistant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity // Extent by @Entity (persistent class)
public class Account extends UserOwnedEntity {

    private String name; // simple attribute

    @Enumerated(EnumType.STRING)
    private Currency currency; // complex attribute

    private BigDecimal balance;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cashflow> cashflows = new ArrayList<>(); // multi-valued attribute, binary association, composition

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_shared_users",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> sharedUsers = new HashSet<>(); // multi-valued attribute, binary association, unique users, ordered collection


}