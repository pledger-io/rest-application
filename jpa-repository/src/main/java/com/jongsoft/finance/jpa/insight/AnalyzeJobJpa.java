package com.jongsoft.finance.jpa.insight;

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
  private String yearMonth;
  private boolean completed;

}
