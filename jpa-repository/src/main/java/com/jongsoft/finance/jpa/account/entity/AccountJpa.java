package com.jongsoft.finance.jpa.account.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Formula;
import com.jongsoft.finance.jpa.core.entity.CurrencyJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.schedule.Periodicity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "account")
public class AccountJpa extends EntityJpa {

    private String name;
    private String description;

    private String iban;
    private String bic;
    private String number;

    private double interest;
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
            double interest,
            Periodicity interestPeriodicity,
            AccountTypeJpa type,
            UserAccountJpa user,
            CurrencyJpa currency,
            boolean archived) {
        super(id);

        this.name = name;
        this.description = description;
        this.iban = iban;
        this.bic = bic;
        this.number = number;
        this.interest = interest;
        this.interestPeriodicity = interestPeriodicity;
        this.type = type;
        this.user = user;
        this.currency = currency;
        this.archived = archived;
    }
}
