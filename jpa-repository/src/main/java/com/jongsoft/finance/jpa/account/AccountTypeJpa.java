package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import lombok.Builder;
import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Getter
@Entity
@Table(name = "account_type")
public class AccountTypeJpa extends EntityJpa {

    @Column(name = "label", length = 150, unique = true)
    private String label;

    private boolean hidden;

    public AccountTypeJpa() {
    }

    @Builder
    protected AccountTypeJpa(Long id, String label, boolean hidden) {
        super(id);
        this.label = label;
        this.hidden = hidden;
    }

    public String getDisplayKey() {
        return "AccountType." + getLabel();
    }

}
