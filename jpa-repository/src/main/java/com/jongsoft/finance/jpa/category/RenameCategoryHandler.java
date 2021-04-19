package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.category.RenameCategoryCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class RenameCategoryHandler implements CommandHandler<RenameCategoryCommand> {

    private final ReactiveEntityManager entityManager;

    public RenameCategoryHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(RenameCategoryCommand command) {
        log.trace("[{}] - Processing rename event for category", command.id());

        entityManager.update()
                .hql("""
                        update CategoryJpa
                        set label = :label,
                            description = :description
                        where id = :id""")
                .set("id", command.id())
                .set("label", command.name())
                .set("description", command.description())
                .execute();
    }

}
