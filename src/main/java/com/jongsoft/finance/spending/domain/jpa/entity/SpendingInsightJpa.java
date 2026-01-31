package com.jongsoft.finance.spending.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.spending.types.InsightType;
import com.jongsoft.finance.spending.types.Severity;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Entity
@Introspected
@Table(name = "spending_insights")
public class SpendingInsightJpa implements WithId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InsightType type;

    private String category;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private double score;
    private LocalDate detectedDate;
    private String message;
    private Long transactionId;

    @Column(name = "year_month_found")
    private String yearMonth;

    @ElementCollection
    @CollectionTable(
            name = "spending_insight_metadata",
            joinColumns = @JoinColumn(name = "insight_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    public SpendingInsightJpa(
            InsightType type,
            String category,
            Severity severity,
            double score,
            LocalDate detectedDate,
            String message,
            YearMonth yearMonth,
            Map<String, String> metadata,
            Long transactionId,
            UserAccountJpa user) {
        this.type = type;
        this.category = category;
        this.severity = severity;
        this.score = score;
        this.detectedDate = detectedDate;
        this.message = message;
        this.transactionId = transactionId;
        this.yearMonth = yearMonth != null ? yearMonth.toString() : null;
        if (metadata != null) {
            this.metadata = metadata;
        }
        this.user = user;
    }

    protected SpendingInsightJpa() {}

    public YearMonth getYearMonth() {
        return yearMonth != null ? YearMonth.parse(yearMonth) : null;
    }

    public Long getId() {
        return id;
    }

    public InsightType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public double getScore() {
        return score;
    }

    public LocalDate getDetectedDate() {
        return detectedDate;
    }

    public String getMessage() {
        return message;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
