package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.core.FailureCode;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.core.entity.AuditedJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Where;

@Entity
@Getter
@Table(name = "transaction_journal")
public class TransactionJournal extends AuditedJpa {

  @Column(name = "t_date", nullable = false, columnDefinition = "DATE")
  private LocalDate date;

  @Column(name = "interest_date", columnDefinition = "DATE")
  private LocalDate interestDate;

  @Column(name = "book_date", columnDefinition = "DATE")
  private LocalDate bookDate;

  private String description;

  @Enumerated(value = EnumType.STRING)
  private TransactionType type;

  @Enumerated(value = EnumType.STRING)
  private FailureCode failureCode;

  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private UserAccountJpa user;

  @ManyToOne @JoinColumn private CategoryJpa category;

  @ManyToOne @JoinColumn private ExpenseJpa budget;

  @ManyToOne @JoinColumn private ContractJpa contract;

  @ManyToOne @JoinColumn private ImportJpa batchImport;

  @ManyToOne @JoinColumn private CurrencyJpa currency;

  @JoinTable(
      name = "transaction_tag",
      joinColumns = @JoinColumn(name = "transaction_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @ManyToMany(fetch = FetchType.EAGER)
  private Set<TagJpa> tags;

  @Where(clause = "deleted is null")
  @OneToMany(mappedBy = "journal", fetch = FetchType.EAGER, orphanRemoval = true)
  private Set<TransactionJpa> transactions;

  public TransactionJournal() {
    super();
  }

  @Builder
  protected TransactionJournal(
      Long id,
      Date created,
      Date updated,
      Date deleted,
      LocalDate date,
      LocalDate bookDate,
      LocalDate interestDate,
      String description,
      TransactionType type,
      FailureCode failureCode,
      UserAccountJpa user,
      CategoryJpa category,
      ExpenseJpa budget,
      ContractJpa contract,
      ImportJpa batchImport,
      CurrencyJpa currency,
      Set<TagJpa> tags,
      Set<TransactionJpa> transactions) {
    super(id, created, updated, deleted);

    this.date = date;
    this.bookDate = bookDate;
    this.interestDate = interestDate;
    this.description = description;
    this.type = type;
    this.failureCode = failureCode;
    this.user = user;
    this.category = category;
    this.budget = budget;
    this.contract = contract;
    this.batchImport = batchImport;
    this.currency = currency;
    this.tags = tags;
    this.transactions = transactions;
  }
}
