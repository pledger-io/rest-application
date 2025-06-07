package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.core.entity.AuditedJpa;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;

/**
 * A TransactionJpa is a part of a transaction journal. Usually a transaction journal contains two
 * transactions, being:
 *
 * <ol>
 *   <li>A debit transaction into one account
 *   <li>A credit transaction into a second account
 * </ol>
 *
 * More advanced examples are split transactions, where you have multiple smaller debit transactions
 * matching one credit transaction.
 */
@Getter
@Entity
@Table(name = "transaction_part")
public class TransactionJpa extends AuditedJpa {

  @ManyToOne
  @JoinColumn(nullable = false)
  private AccountJpa account;

  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private TransactionJournal journal;

  private BigDecimal amount;
  private String description;

  public TransactionJpa() {}

  @Builder
  protected TransactionJpa(
      Long id,
      Date created,
      Date updated,
      Date deleted,
      AccountJpa account,
      TransactionJournal journal,
      BigDecimal amount,
      String description) {
    super(id, created, updated, deleted);

    this.account = account;
    this.journal = journal;
    this.amount = amount;
    this.description = description;
  }
}
