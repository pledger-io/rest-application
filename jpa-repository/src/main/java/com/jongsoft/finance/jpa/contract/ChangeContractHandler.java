package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.ChangeContractCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class ChangeContractHandler implements CommandHandler<ChangeContractCommand> {

    private final ReactiveEntityManager entityManager;

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
