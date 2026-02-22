package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.CurrencyJpa;
import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.Periodicity;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Introspected
@Table(name = "account")
public class AccountJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private String description;

    private String iban;
    private String bic;
    private String number;

    private String imageFileToken;

    private double interest;

    @Enumerated(value = EnumType.STRING)
    private Periodicity interestPeriodicity;

    @ManyToOne
    private AccountTypeJpa type;

    @ManyToOne
    private UserAccountJpa user;

    @ManyToOne
    private CurrencyJpa currency;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(select max(tj.t_date) from transaction_part t join transaction_journal tj on tj.id ="
            + " t.journal_id where t.account_id = id and t.deleted is null)")
    private LocalDate lastTransaction;

    @Basic(fetch = FetchType.LAZY)
    @Formula("(select min(tj.t_date) from transaction_part t join transaction_journal tj on tj.id ="
            + " t.journal_id where t.account_id = id and t.deleted is null)")
    private LocalDate firstTransaction;

    private boolean archived;

    @SQLRestriction("archived = false")
    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<SavingGoalJpa> savingGoals = Set.of();

    public AccountJpa() {}

    private AccountJpa(
            String name, AccountTypeJpa type, UserAccountJpa user, CurrencyJpa currency) {
        this.name = name;
        this.type = type;
        this.user = user;
        this.currency = currency;
        this.archived = false;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getNumber() {
        return number;
    }

    public String getImageFileToken() {
        return imageFileToken;
    }

    public double getInterest() {
        return interest;
    }

    public Periodicity getInterestPeriodicity() {
        return interestPeriodicity;
    }

    public AccountTypeJpa getType() {
        return type;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public CurrencyJpa getCurrency() {
        return currency;
    }

    public LocalDate getLastTransaction() {
        return lastTransaction;
    }

    public LocalDate getFirstTransaction() {
        return firstTransaction;
    }

    public boolean isArchived() {
        return archived;
    }

    public Set<SavingGoalJpa> getSavingGoals() {
        return savingGoals;
    }

    public static AccountJpa of(
            String name, AccountTypeJpa type, UserAccountJpa user, CurrencyJpa currency) {
        return new AccountJpa(name, type, user, currency);
    }
}
