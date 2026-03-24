package com.jongsoft.finance.invoice.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Introspected
@Table(name = "tax_bracket")
public class TaxBracketJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    private String name;
    private BigDecimal rate;

    @ManyToOne
    private UserAccountJpa user;

    public TaxBracketJpa() {}

    private TaxBracketJpa(String name, BigDecimal rate, UserAccountJpa user) {
        this.name = name;
        this.rate = rate;
        this.user = user;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public static TaxBracketJpa of(String name, BigDecimal rate, UserAccountJpa user) {
        return new TaxBracketJpa(name, rate, user);
    }
}
