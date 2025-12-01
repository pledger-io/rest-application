package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "contract")
public class ContractJpa extends EntityJpa {

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    private String fileToken;

    @ManyToOne
    private AccountJpa company;

    @ManyToOne
    private UserAccountJpa user;

    private boolean warningActive;
    private boolean archived;

    public ContractJpa() {}

    @Builder
    protected ContractJpa(
            Long id,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String fileToken,
            AccountJpa company,
            UserAccountJpa user,
            boolean warningActive,
            boolean archived) {
        super(id);
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileToken = fileToken;
        this.company = company;
        this.user = user;
        this.warningActive = warningActive;
        this.archived = archived;
    }
}
