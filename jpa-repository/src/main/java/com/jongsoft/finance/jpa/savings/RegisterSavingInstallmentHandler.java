package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.RegisterSavingInstallmentCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@AllArgsConstructor(onConstructor_ = @Inject)
public class RegisterSavingInstallmentHandler implements CommandHandler<RegisterSavingInstallmentCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    public void handle(RegisterSavingInstallmentCommand command) {
        log.info("[{}] - Incrementing allocation for saving goal.", command.id());

        var hql = """
                update SavingGoalJpa
                set
                  allocated = allocated + :installment
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("installment", command.amount())
                .set("id", command.id())
                .execute();
    }
}
