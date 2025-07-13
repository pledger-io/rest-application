package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.domain.insight.PatternType;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "spending_patterns")
public class SpendingPatternJpa extends EntityJpa {

  static final String COLUMN_CATEGORY = "category";
  static final String COLUMN_USERNAME = "user.username";
  static final String COLUMN_YEAR_MONTH = "yearMonth";

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

  @Builder
  private SpendingPatternJpa(
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
}
