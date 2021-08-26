package com.jongsoft.finance.jpa.transaction;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.core.TransactionType;
import com.jongsoft.finance.domain.transaction.Transaction;
import com.jongsoft.finance.jpa.account.AccountJpa;
import com.jongsoft.finance.jpa.budget.ExpenseJpa;
import com.jongsoft.finance.jpa.category.CategoryJpa;
import com.jongsoft.finance.jpa.contract.ContractJpa;
import com.jongsoft.finance.jpa.currency.CurrencyJpa;
import com.jongsoft.finance.jpa.importer.entity.ImportJpa;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.jpa.tag.TagJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.transaction.CreateTransactionCommand;
import com.jongsoft.finance.messaging.handlers.TransactionCreationHandler;
import com.jongsoft.finance.security.AuthenticationFacade;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashSet;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CreateTransactionHandler implements CommandHandler<CreateTransactionCommand>, TransactionCreationHandler {

    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Override
    @BusinessEventListener
    public void handle(CreateTransactionCommand command) {
        handleCreatedEvent(command);
    }

    @Override
    public long handleCreatedEvent(CreateTransactionCommand command) {
        log.info("[{}] - Processing transaction create event", command.transaction().getDescription());

        var jpaEntity = TransactionJournal.builder()
                .date(command.transaction().getDate())
                .bookDate(command.transaction().getBookDate())
                .interestDate(command.transaction().getInterestDate())
                .description(command.transaction().getDescription())
                .currency(entityManager.get(CurrencyJpa.class, Collections.Map("code", command.transaction().getCurrency())))
                .user(entityManager.get(UserAccountJpa.class, Collections.Map("username", authenticationFacade.authenticated())))
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
                .batchImport(Control.Option(command.transaction().getImportSlug())
                        .map(this::job)
                        .getOrSupply(() -> null))
                .build();

        entityManager.persist(jpaEntity);

        for (Transaction.Part transfer : command.transaction().getTransactions()) {
            // todo change to native BigDecimal later on
            var transferJpa = TransactionJpa.builder()
                    .amount(BigDecimal.valueOf(transfer.getAmount()))
                    .account(entityManager.get(AccountJpa.class, Collections.Map("id", transfer.getAccount().getId())))
                    .journal(jpaEntity)
                    .build();

            jpaEntity.getTransactions().add(transferJpa);
            entityManager.persist(transferJpa);
        }

        return jpaEntity.getId();
    }

    private CategoryJpa category(String label) {
        var hql = """
                select c from CategoryJpa c 
                where c.label = :label and c.user.username = :username""";
        return entityManager.<CategoryJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("label", label)
                .maybe()
                .getOrSupply(() -> null);
    }

    private ExpenseJpa expense(String name) {
        var hql = """
                select e from ExpenseJpa e
                where e.name = :name and e.user.username = :username""";

        return entityManager.<ExpenseJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrSupply(() -> null);
    }

    private ContractJpa contract(String name) {
        var hql = """
                select e from ContractJpa e
                where e.name = :name and e.user.username = :username""";

        return entityManager.<ContractJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrSupply(() -> null);
    }

    private ImportJpa job(String slug) {
        var hql = """
                select e from ImportJpa e
                where e.slug = :slug and e.user.username = :username""";

        return entityManager.<ImportJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("slug", slug)
                .maybe()
                .getOrSupply(() -> null);
    }

    private TagJpa tag(String name) {
        var hql = """
                select t from TagJpa t
                where t.name = :name and t.user.username = :username""";

        return entityManager.<TagJpa>blocking()
                .hql(hql)
                .set("username", authenticationFacade.authenticated())
                .set("name", name)
                .maybe()
                .getOrSupply(() -> null);
    }

}
