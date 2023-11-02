package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.savings.SavingGoalJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.schedule.Periodicity;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Formula;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "account")
public class AccountJpa extends EntityJpa {

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

    @Formula("(select max(tj.t_date) from transaction_part t join transaction_journal tj on tj.id = t.journal_id where t.account_id = id and t.deleted is null)")
    private LocalDate lastTransaction;

    @Formula("(select min(tj.t_date) from transaction_part t join transaction_journal tj on tj.id = t.journal_id where t.account_id = id and t.deleted is null)")
    private LocalDate firstTransaction;

    @Formula("(select sum(t.amount) from transaction_part t where t.account_id = id and t.deleted is null)")
    private Double balance;

    private boolean archived;

    @OneToMany(mappedBy = "account", fetch = FetchType.EAGER)
    private Set<SavingGoalJpa> savingGoals = Set.of();

    public AccountJpa() {
    }

    @Builder
    protected AccountJpa(
            Long id,
            String name,
            String description,
            String iban,
            String bic,
            String number,
            String imageFileToken,
            double interest,
            Periodicity interestPeriodicity,
            AccountTypeJpa type,
            UserAccountJpa user,
            CurrencyJpa currency,
            boolean archived,
            Set<SavingGoalJpa> savingGoals) {
        super(id);

        this.name = name;
        this.description = description;
        this.iban = iban;
        this.bic = bic;
        this.number = number;
        this.imageFileToken = imageFileToken;
        this.interest = interest;
        this.interestPeriodicity = interestPeriodicity;
        this.type = type;
        this.user = user;
        this.currency = currency;
        this.archived = archived;
        this.savingGoals = savingGoals != null ? new HashSet<>(savingGoals) : new HashSet<>();
    }
}
