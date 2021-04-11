package com.jongsoft.finance.jpa.contract;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.contract.AttachFileToContractCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class AttacheFileToContractHandler implements CommandHandler<AttachFileToContractCommand> {

    private final ReactiveEntityManager entityManager;

    public AttacheFileToContractHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(AttachFileToContractCommand command) {
        log.trace("[{}] - Processing contract upload event", command.id());

        var hql = """
                update ContractJpa
                set fileToken = :token
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("token", command.fileCode())
                .set("id", command.id())
                .update();
    }

}
