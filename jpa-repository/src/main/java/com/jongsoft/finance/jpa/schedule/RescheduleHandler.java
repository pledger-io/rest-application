package com.jongsoft.finance.jpa.schedule;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.schedule.RescheduleCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class RescheduleHandler implements CommandHandler<RescheduleCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(RescheduleCommand command) {
        log.info("[{}] - Processing schedule reschedule event", command.id());

        var hql = """
                update ScheduledTransactionJpa 
                set interval = :interval,
                    periodicity = :periodicity
                where id = :id""";

        entityManager.update()
                .hql(hql)
                .set("id", command.id())
                .set("interval", command.schedule().interval())
                .set("periodicity", command.schedule().periodicity())
                .execute();
    }

}
