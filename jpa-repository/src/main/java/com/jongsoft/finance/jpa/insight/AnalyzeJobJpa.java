package com.jongsoft.finance.jpa.insight;

import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import jakarta.persistence.*;

import lombok.Data;

@Data
@Entity
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
}
