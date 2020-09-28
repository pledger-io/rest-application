package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.domain.FilterFactory;
import com.jongsoft.finance.domain.core.ResultPage;
import com.jongsoft.finance.domain.core.SettingProvider;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.domain.transaction.TagProvider;
import com.jongsoft.finance.domain.transaction.events.TagCreatedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class TransactionTagResourceTest extends TestSetup {

    private TransactionTagResource subject;

    @Mock
    private SettingProvider settingProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private TagProvider tagProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    private FilterFactory filterFactory;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();

        subject = new TransactionTagResource(
                settingProvider,
                tagProvider,
                filterFactory,
                currentUserProvider);

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);

        new EventBus(eventPublisher);
    }

    @Test
    void create() {
        var response = subject.create("Sample tag").blockingGet();

        Assertions.assertThat(response.getName()).isEqualTo("Sample tag");

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(TagCreatedEvent.class));
    }

    @Test
    void autoCompleteTag() {
        Mockito.when(tagProvider.lookup(Mockito.any(TagProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(new Tag("Sample")));

        var response = subject.autoCompleteTag("samp").test();

        response.assertComplete();
        response.assertValueCount(1);

        var mockFilter = filterFactory.tag();
        Mockito.verify(tagProvider).lookup(Mockito.any(TagProvider.FilterCommand.class));
        Mockito.verify(mockFilter).name("samp", false);
    }
}