package com.jongsoft.finance.domain.transaction;

import static org.assertj.core.api.Assertions.*;

import com.jongsoft.finance.messaging.commands.rule.RenameRuleGroupCommand;
import com.jongsoft.finance.messaging.commands.rule.ReorderRuleGroupCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

class TransactionRuleGroupTest {

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);
    }

    @Test
    void changeOrder() {
        var captor = ArgumentCaptor.forClass(ReorderRuleGroupCommand.class);

        TransactionRuleGroup.builder()
                .sort(1)
                .name("Test Group")
                .id(1L)
                .build()
                .changeOrder(2);

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1L);
        assertThat(captor.getValue().sort()).isEqualTo(2);
    }

    @Test
    void rename() {
        var captor = ArgumentCaptor.forClass(RenameRuleGroupCommand.class);

        TransactionRuleGroup.builder()
                .sort(1)
                .name("Test Group")
                .id(1L)
                .build()
                .rename("Changed group");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1L);
        assertThat(captor.getValue().name()).isEqualTo("Changed group");
    }

}
