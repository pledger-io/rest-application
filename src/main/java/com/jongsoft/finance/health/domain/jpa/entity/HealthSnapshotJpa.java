package com.jongsoft.finance.health.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.health.types.HealthRating;
import com.jongsoft.finance.health.types.HealthSnapshotStatus;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Currency;

@Entity
@Introspected
@Table(
        name = "financial_health_snapshot",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_financial_health_user_period",
                        columnNames = {"user_id", "year_month_period"}))
public class HealthSnapshotJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserAccountJpa user;

    @Column(name = "year_month_period", nullable = false, length = 7)
    private String yearMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private HealthSnapshotStatus status;

    @Column(name = "score")
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", length = 32)
    private HealthRating rating;

    @Column(name = "window_months", nullable = false)
    private int windowMonths;

    @Column(name = "algorithm_version", nullable = false)
    private int algorithmVersion;

    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "savings_rate", precision = 9, scale = 4)
    private BigDecimal savingsRate;

    @Column(name = "savings_score")
    private Integer savingsScore;

    @Column(name = "debt_to_income", precision = 9, scale = 4)
    private BigDecimal debtToIncome;

    @Column(name = "dti_score")
    private Integer dtiScore;

    @Column(name = "budget_adherence", precision = 9, scale = 4)
    private BigDecimal budgetAdherence;

    @Column(name = "budget_score")
    private Integer budgetScore;

    @Column(name = "income", precision = 22, scale = 2)
    private BigDecimal income;

    @Column(name = "expenses", precision = 22, scale = 2)
    private BigDecimal expenses;

    @Column(name = "debt_payments", precision = 22, scale = 2)
    private BigDecimal debtPayments;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    protected HealthSnapshotJpa() {}

    public HealthSnapshotJpa(
            UserAccountJpa user,
            YearMonth yearMonth,
            HealthSnapshotStatus status,
            Integer score,
            HealthRating rating,
            int windowMonths,
            int algorithmVersion,
            Currency currency,
            BigDecimal savingsRate,
            Integer savingsScore,
            BigDecimal debtToIncome,
            Integer dtiScore,
            BigDecimal budgetAdherence,
            Integer budgetScore,
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal debtPayments,
            LocalDateTime computedAt) {
        this.user = user;
        this.yearMonth = yearMonth != null ? yearMonth.toString() : null;
        this.status = status;
        this.score = score;
        this.rating = rating;
        this.windowMonths = windowMonths;
        this.algorithmVersion = algorithmVersion;
        this.currency = currency;
        this.savingsRate = savingsRate;
        this.savingsScore = savingsScore;
        this.debtToIncome = debtToIncome;
        this.dtiScore = dtiScore;
        this.budgetAdherence = budgetAdherence;
        this.budgetScore = budgetScore;
        this.income = income;
        this.expenses = expenses;
        this.debtPayments = debtPayments;
        this.computedAt = computedAt;
    }

    @Override
    public Long getId() {
        return id;
    }

    public UserAccountJpa getUser() {
        return user;
    }

    public YearMonth getYearMonth() {
        return yearMonth != null ? YearMonth.parse(yearMonth) : null;
    }

    public HealthSnapshotStatus getStatus() {
        return status;
    }

    public Integer getScore() {
        return score;
    }

    public HealthRating getRating() {
        return rating;
    }

    public int getWindowMonths() {
        return windowMonths;
    }

    public int getAlgorithmVersion() {
        return algorithmVersion;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public Integer getSavingsScore() {
        return savingsScore;
    }

    public BigDecimal getDebtToIncome() {
        return debtToIncome;
    }

    public Integer getDtiScore() {
        return dtiScore;
    }

    public BigDecimal getBudgetAdherence() {
        return budgetAdherence;
    }

    public Integer getBudgetScore() {
        return budgetScore;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpenses() {
        return expenses;
    }

    public BigDecimal getDebtPayments() {
        return debtPayments;
    }

    public LocalDateTime getComputedAt() {
        return computedAt;
    }
}
