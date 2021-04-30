package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.TerminateContractCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class TerminateContractHandler implements CommandHandler<TerminateContractCommand> {

    private final ReactiveEntityManager entityManager;

    public TerminateContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(TerminateContractCommand command) {
        log.info("[{}] - Processing contract terminate event", command.id());

        var hql = """
                update ContractJpa c
                set c.archived = true
                where c.id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .execute();
    }

}
