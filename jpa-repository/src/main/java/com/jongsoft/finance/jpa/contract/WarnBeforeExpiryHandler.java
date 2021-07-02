package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.WarnBeforeExpiryCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WarnBeforeExpiryHandler implements CommandHandler<WarnBeforeExpiryCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(WarnBeforeExpiryCommand command) {
        log.info("[{}] - Processing contract warning event", command.id());

        var hql = """
                update ContractJpa
                set warningActive = true,
                    endDate = :endDate
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("endDate", command.endDate())
                .execute();
    }

}
