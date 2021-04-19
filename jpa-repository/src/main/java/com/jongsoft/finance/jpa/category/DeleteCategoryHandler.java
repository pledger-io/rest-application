package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import javax.transaction.Transactional;

@Slf4j
@Singleton
@Transactional
public class DeleteCategoryHandler implements CommandHandler<DeleteCategoryCommand> {

    private final ReactiveEntityManager entityManager;

    public DeleteCategoryHandler(ReactiveEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @BusinessEventListener
    public void handle(DeleteCategoryCommand command) {
        log.trace("[{}] - Processing remove event for category", command.id());

        entityManager.update()
                .hql("""
                        update CategoryJpa
                        set archived = true
                        where id = :id""")
                .set("id", command.id())
                .execute();
    }

}
