package com.jongsoft.finance.domain.transaction;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupRenamedEvent;
import com.jongsoft.finance.domain.transaction.events.TransactionRuleGroupSortedEvent;
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
        ArgumentCaptor<TransactionRuleGroupSortedEvent> captor = ArgumentCaptor.forClass(TransactionRuleGroupSortedEvent.class);

        TransactionRuleGroup.builder()
                .sort(1)
                .name("Test Group")
                .id(1L)
                .build()
                .changeOrder(2);

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getGroupId()).isEqualTo(1L);
        assertThat(captor.getValue().getSortOrder()).isEqualTo(2);
    }

    @Test
    void rename() {
        ArgumentCaptor<TransactionRuleGroupRenamedEvent> captor = ArgumentCaptor.forClass(TransactionRuleGroupRenamedEvent.class);

        TransactionRuleGroup.builder()
                .sort(1)
                .name("Test Group")
                .id(1L)
                .build()
                .rename("Changed group");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getRuleGroupId()).isEqualTo(1L);
        assertThat(captor.getValue().getName()).isEqualTo("Changed group");
    }

}
