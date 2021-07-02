package com.jongsoft.finance.jpa.account;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.account.ChangeInterestCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeAccountInterestHandler implements CommandHandler<ChangeInterestCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(ChangeInterestCommand command) {
        log.info("[{}] - Processing account interest event", command.id());

        var hql = """
                update AccountJpa 
                set interest = :interest,
                    interestPeriodicity = :periodicity
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("interest", command.interest())
                .set("periodicity", command.periodicity())
                .execute();
    }

}
