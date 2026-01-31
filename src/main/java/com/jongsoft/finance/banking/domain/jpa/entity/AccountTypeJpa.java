package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

@Entity
@Table(name = "account_type")
public class AccountTypeJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "label", length = 150, unique = true)
    private String label;

    private boolean hidden;

    public AccountTypeJpa() {}

    private AccountTypeJpa(Long id, String label, boolean hidden) {
        this.id = id;
        this.label = label;
        this.hidden = hidden;
    }

    public String getDisplayKey() {
        return "AccountType." + label;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public boolean isHidden() {
        return hidden;
    }

    public static AccountTypeJpa of(Long id, String label, boolean hidden) {
        return new AccountTypeJpa(id, label, hidden);
    }
}
