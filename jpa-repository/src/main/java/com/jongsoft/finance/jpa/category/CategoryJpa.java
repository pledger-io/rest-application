package com.jongsoft.finance.jpa.category;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import org.hibernate.annotations.Formula;
import com.jongsoft.finance.jpa.core.entity.EntityJpa;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "category")
public class CategoryJpa extends EntityJpa {

    private String label;
    private String description;
    private boolean archived;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private UserAccountJpa user;

    @Formula("(select max(tj.t_date) from transaction_journal tj where tj.category_id = id and tj.deleted is null)")
    private LocalDate lastTransaction;

    @Builder
    private CategoryJpa(
            String label,
            String description,
            boolean archived,
            UserAccountJpa user,
            LocalDate lastTransaction) {
        this.label = label;
        this.description = description;
        this.archived = archived;
        this.user = user;
        this.lastTransaction = lastTransaction;
    }

    protected CategoryJpa() {
    }
}
