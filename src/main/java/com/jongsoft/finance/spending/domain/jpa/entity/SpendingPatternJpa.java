package com.jongsoft.finance.spending.domain.jpa.entity;

import com.jongsoft.finance.core.domain.jpa.entity.UserAccountJpa;
import com.jongsoft.finance.core.value.WithId;
import com.jongsoft.finance.spending.types.PatternType;

import io.micronaut.core.annotation.Introspected;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Entity
@Introspected
@Table(name = "spending_patterns")
public class SpendingPatternJpa implements WithId {

    static final String COLUMN_CATEGORY = "category";
    static final String COLUMN_USERNAME = "user.username";
    static final String COLUMN_YEAR_MONTH = "yearMonth";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PatternType type;

    private String category;
    private double confidence;
    private LocalDate detectedDate;

    @Column(name = "year_month_found")
    private String yearMonth;

    @ElementCollection
    @CollectionTable(
            name = "spending_pattern_metadata",
            joinColumns = @JoinColumn(name = "pattern_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    public SpendingPatternJpa(
            PatternType type,
            String category,
            double confidence,
            LocalDate detectedDate,
            YearMonth yearMonth,
            Map<String, String> metadata,
            UserAccountJpa user) {
        this.type = type;
        this.category = category;
        this.confidence = confidence;
        this.detectedDate = detectedDate;
        this.yearMonth = yearMonth != null ? yearMonth.toString() : null;
        if (metadata != null) {
            this.metadata = metadata;
        }
        this.user = user;
    }

    protected SpendingPatternJpa() {}

    public YearMonth getYearMonth() {
        return yearMonth != null ? YearMonth.parse(yearMonth) : null;
    }

    public Long getId() {
        return id;
    }

    public PatternType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getConfidence() {
        return confidence;
    }

    public LocalDate getDetectedDate() {
        return detectedDate;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public UserAccountJpa getUser() {
        return user;
    }
}
