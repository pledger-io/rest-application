package com.jongsoft.finance.banking.domain.jpa.entity;

import com.jongsoft.finance.core.value.WithId;

import jakarta.persistence.*;

@Entity
@Table(name = "account_synonym")
public class AccountSynonymJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String synonym;

    @ManyToOne
    private AccountJpa account;

    public AccountSynonymJpa(String synonym, AccountJpa account) {
        this.synonym = synonym;
        this.account = account;
    }

    public AccountSynonymJpa() {}

    @Override
    public Long getId() {
        return id;
    }

    public String getSynonym() {
        return synonym;
    }

    public AccountJpa getAccount() {
        return account;
    }
}
