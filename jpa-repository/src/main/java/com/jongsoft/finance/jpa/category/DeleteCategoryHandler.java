package com.jongsoft.finance.jpa.category;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.jpa.reactive.ReactiveEntityManager;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class DeleteCategoryHandler implements CommandHandler<DeleteCategoryCommand> {

    private final ReactiveEntityManager entityManager;

    @Override
    @BusinessEventListener
    public void handle(DeleteCategoryCommand command) {
        log.info("[{}] - Processing remove event for category", command.id());

        entityManager.update()
                .hql("""
                        update CategoryJpa
                        set archived = true
                        where id = :id""")
                .set("id", command.id())
                .execute();
    }

}
