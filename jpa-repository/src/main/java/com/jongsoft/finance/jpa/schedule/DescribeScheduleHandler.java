package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.DescribeScheduleCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class DescribeScheduleHandler implements CommandHandler<DescribeScheduleCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public DescribeScheduleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DescribeScheduleCommand command) {
        log.info("[{}] - Processing schedule describe event", command.id());

        var hql = """
                update ScheduledTransactionJpa
                set description = :description,
                    name = :name
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("description", command.description())
                .set("name", command.name())
                .execute();
    }
}
