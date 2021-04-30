package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class ChangeContractHandler implements CommandHandler<ChangeContractCommand> {

    private final ReactiveEntityManager entityManager;

    public ChangeContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(ChangeContractCommand command) {
        log.info("[{}] - Processing contract changed event", command.id());

        var hql = """
                update ContractJpa
                set name = :name,
                    startDate = :startDate,
                    endDate = :endDate,
                    description = :description
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("name", command.name())
                .set("startDate", command.start())
                .set("endDate", command.end())
                .set("description", command.description())
                .set("id", command.id())
                .execute();
    }

}
