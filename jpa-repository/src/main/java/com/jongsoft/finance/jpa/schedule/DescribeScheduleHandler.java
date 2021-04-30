package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.DescribeScheduleCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class DescribeScheduleHandler implements CommandHandler<DescribeScheduleCommand> {

    private final ReactiveEntityManager entityManager;

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
