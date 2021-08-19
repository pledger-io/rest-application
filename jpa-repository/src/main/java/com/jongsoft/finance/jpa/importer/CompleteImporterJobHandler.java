package com.jongsoft.finance.jpa.importer;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.importer.CompleteImportJobCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class CompleteImporterJobHandler implements CommandHandler<CompleteImportJobCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(CompleteImportJobCommand command) {
        log.info("[{}] - Processing import finished event", command.id());

        var hql = """
                update ImportJpa
                set finished = :finished
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("finished", new Date())
                .execute();
    }

}
