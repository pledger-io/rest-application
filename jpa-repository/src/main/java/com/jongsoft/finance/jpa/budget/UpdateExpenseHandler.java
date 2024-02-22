package com.jongsoft.finance.jpa.budget;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.budget.UpdateExpenseCommand;
import com.jongsoft.finance.security.AuthenticationFacade;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Singleton
@Transactional
public class UpdateExpenseHandler implements CommandHandler<UpdateExpenseCommand> {
    private final ReactiveEntityManager entityManager;
    private final AuthenticationFacade authenticationFacade;

    @Inject
    public UpdateExpenseHandler(ReactiveEntityManager entityManager, AuthenticationFacade authenticationFacade) {
        this.entityManager = entityManager;
        this.authenticationFacade = authenticationFacade;
    }

    @Override
    @BusinessEventListener
    public void handle(UpdateExpenseCommand command) {

        var hql = """
                from ExpensePeriodJpa e
                where
                    e.expense.id = :id
                    and e.budget.until is null
                    and e.budget.user.username = :username""";

        var existing = entityManager.<ExpensePeriodJpa>blocking()
                .hql(hql)
                .set("id", command.id())
                .set("username", authenticationFacade.authenticated())
                .maybe()
                .getOrThrow(() -> new RuntimeException("Unable to find expense"));

        log.info("[{}] - Processing expense update event", existing.getId());

        existing.setLowerBound(command.amount().subtract(BigDecimal.valueOf(.01)));
        existing.setUpperBound(command.amount());
        entityManager.persist(existing);
    }
}
