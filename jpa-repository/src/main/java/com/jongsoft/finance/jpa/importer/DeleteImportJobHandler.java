package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class DeleteImportJobHandler implements CommandHandler<DeleteImportJobCommand> {

    private final ReactiveEntityManager entityManager;

    public DeleteImportJobHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DeleteImportJobCommand command) {
        log.info("[{}] - Processing import deleted event", command.id());

        var hql = """
                update ImportJpa
                set archived = true
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .execute();
    }

}
