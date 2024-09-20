package com.jongsoft.finance.jpa.savings;

import com.jongsoft.finance.RequiresJpa;
import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.savings.AdjustScheduleCommand;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiresJpa
@Transactional
public class AdjustScheduleHandler implements CommandHandler<AdjustScheduleCommand> {

    private final ReactiveEntityManager entityManager;

    public AdjustScheduleHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(AdjustScheduleCommand command) {
        log.info("[{}] - Adjusting schedule for a saving goal.", command.id());

        var hql = """
                update SavingGoalJpa
                set
                  targetDate = :end,
                  periodicity = :periodicity,
                  interval = :interval
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("end", command.schedulable().getEnd())
                .set("periodicity", command.schedulable().getSchedule().periodicity())
                .set("interval", command.schedulable().getSchedule().interval())
                .execute();
    }

}
