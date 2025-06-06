package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.query.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.messaging.notifications.TransactionCreated;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.math.BigDecimal;
import java.util.HashSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class CreateTransactionHandler
    implements CommandHandler<CreateTransactionCommand>, TransactionCreationHandler {

  private final ReactiveEntityManager entityManager;
  private final AuthenticationFacade authenticationFacade;

  @Inject
  public CreateTransactionHandler(
      ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
    this.entityManager = entityManager;
    this.authenticationFacade = authenticationFacade;
  }

  @Override
  @BusinessEventListener
  public void handle(CreateTransactionCommand command) {
    handleCreatedEvent(command);
  }

  @Override
  public long handleCreatedEvent(CreateTransactionCommand command) {
    log.info("[{}] - Processing transaction create event", command.transaction().getDescription());

    var jpaEntity =
        TransactionJournal.builder()
            .date(command.transaction().getDate())
            .bookDate(command.transaction().getBookDate())
            .interestDate(command.transaction().getInterestDate())
            .description(command.transaction().getDescription())
            .currency(
                entityManager
                    .from(CurrencyJpa.class)
                    .fieldEq("code", command.transaction().getCurrency())
                    .singleResult()
                    .get())
            .user(entityManager.currentUser())
            .type(TransactionType.valueOf(command.transaction().computeType().name()))
            .failureCode(command.transaction().getFailureCode())
            .transactions(new HashSet<>())
            .category(
                Control.Option(command.transaction().getCategory())
                    .map(this::category)
                    .getOrSupply(() -> null))
            .budget(
                Control.Option(command.transaction().getBudget())
                    .map(this::expense)
                    .getOrSupply(() -> null))
            .contract(
                Control.Option(command.transaction().getContract())
                    .map(this::contract)
                    .getOrSupply(() -> null))
            .tags(
                Control.Option(command.transaction().getTags())
                    .map(Sequence::distinct)
                    .map(set -> set.map(this::tag).toJava())
                    .getOrSupply(() -> null))
            .batchImport(
                Control.Option(command.transaction().getImportSlug())
                    .map(this::job)
                    .getOrSupply(() -> null))
            .build();

    entityManager.persist(jpaEntity);

    for (Transaction.Part transfer : command.transaction().getTransactions()) {
      // todo change to native BigDecimal later on
      var transferJpa =
          TransactionJpa.builder()
              .amount(BigDecimal.valueOf(transfer.getAmount()))
              .account(entityManager.getById(AccountJpa.class, transfer.getAccount().getId()))
              .journal(jpaEntity)
              .build();

      jpaEntity.getTransactions().add(transferJpa);
      entityManager.persist(transferJpa);
    }

    TransactionCreated.transactionCreated(jpaEntity.getId());
    return jpaEntity.getId();
  }

  private CategoryJpa category(String label) {
    return entityManager
        .from(CategoryJpa.class)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .fieldEq("label", label)
        .singleResult()
        .getOrSupply(() -> null);
  }

  private ExpenseJpa expense(String name) {
    return entityManager
        .from(ExpenseJpa.class)
        .fieldEq("name", name)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .getOrSupply(() -> null);
  }

  private ContractJpa contract(String name) {
    return entityManager
        .from(ContractJpa.class)
        .fieldEq("name", name)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .getOrSupply(() -> null);
  }

  private ImportJpa job(String slug) {
    return entityManager
        .from(ImportJpa.class)
        .fieldEq("slug", slug)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .getOrSupply(() -> null);
  }

  private TagJpa tag(String name) {
    return entityManager
        .from(TagJpa.class)
        .fieldEq("name", name)
        .fieldEq("user.username", authenticationFacade.authenticated())
        .singleResult()
        .getOrSupply(() -> null);
  }
}
