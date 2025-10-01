package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.domain.insight.InsightType;
import com.jongsoft.finance.domain.insight.Severity;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import jakarta.persistence.*;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Getter
@Entity
@Table(name = "spending_insights")
public class SpendingInsightJpa extends EntityJpa {

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

    @Builder
    private SpendingInsightJpa(
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
}
