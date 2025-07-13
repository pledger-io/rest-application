package com.jongsoft.finance.jpa.insight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "analyze_job")
public class AnalyzeJobJpa {

  @Id
  private String id;

  @Column(name = "year_month_found")
  private String yearMonth;
  private boolean completed;
}
