package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.LimitScheduleCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class LimitScheduleHandler implements CommandHandler<LimitScheduleCommand> {

    private final ReactiveEntityManager entityManager;

    @Inject
    public LimitScheduleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(LimitScheduleCommand command) {
        log.info("[{}] - Processing schedule limit event", command.id());

        var hql = """
                update ScheduledTransactionJpa t
                set t.start = :startDate,
                    t.end = :endDate
                where t.id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("startDate", command.start())
                .set("endDate", command.end())
                .execute();
    }

}
