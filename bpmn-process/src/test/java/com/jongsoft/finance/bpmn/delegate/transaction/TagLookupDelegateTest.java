package com.jongsoft.finance.bpmn.delegate.transaction;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.domain.transaction.events.TagCreatedEvent;
import com.jongsoft.finance.domain.user.Role;
import com.jongsoft.finance.domain.user.UserAccount;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import io.reactivex.Maybe;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

class TagLookupDelegateTest {

    private TagLookupDelegate subject;

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private TagProvider tagProvider;
    @Mock
    private DelegateExecution execution;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        subject = new TagLookupDelegate(currentUserProvider, tagProvider);

        new EventBus(eventPublisher);
    }

    @Test
    void execute() throws Exception {
        var tag = new Tag("Auto");

        BDDMockito.given(execution.getVariableLocalTyped("name"))
                .willReturn(new PrimitiveTypeValueImpl.StringValueImpl("Auto"));
        BDDMockito.given(tagProvider.lookup("Auto")).willReturn(Maybe.just(tag));

        subject.execute(execution);

        BDDMockito.verify(execution).setVariableLocal("id", "Auto");
    }

    @Test
    void execute_createNew() throws Exception {
        var tag = new Tag("Auto");
        var userAccount = UserAccount.builder()
                .id(1L)
                .username("test-user")
                .password("12345")
                .roles(Collections.List(new Role("admin")))
                .build();

        BDDMockito.given(execution.getVariableLocalTyped("name"))
                .willReturn(new PrimitiveTypeValueImpl.StringValueImpl("Auto"));
        BDDMockito.given(currentUserProvider.currentUser()).willReturn(userAccount);
        BDDMockito.given(tagProvider.lookup("Auto"))
                .willReturn(Maybe.empty())
                .willReturn(Maybe.just(tag));

        subject.execute(execution);

        BDDMockito.verify(execution).setVariableLocal("id", "Auto");
        BDDMockito.verify(eventPublisher).publishEvent(Mockito.any(TagCreatedEvent.class));
    }
}
