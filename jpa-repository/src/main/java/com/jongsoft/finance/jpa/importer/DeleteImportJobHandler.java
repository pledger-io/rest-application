package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.DeleteImportJobCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DeleteImportJobHandler implements CommandHandler<DeleteImportJobCommand> {

    private final ReactiveEntityManager entityManager;
    
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
