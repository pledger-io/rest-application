package com.jongsoft.finance.spending.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

@Entity
@Introspected
@Table(name = "analyze_job")
public class AnalyzeJobJpa {

    @Id
    private String id;

    @Column(name = "year_month_found")
    private String yearMonth;

    @ManyToOne
    private UserAccountJpa user;

    private boolean completed;
    private boolean failed;

    public String getId() {
        return id;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setId(String string) {
        this.id = string;
    }

    public void setUser(UserAccountJpa user) {
        this.user = user;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }
}
