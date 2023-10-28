package com.jongsoft.finance.rest.transaction;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.tag.CreateTagCommand;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TagProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.model.TagResponse;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

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
        Assertions.assertThat(subject.create(new TagCreateRequest("Sample tag")))
                .isNotNull()
                .isInstanceOfSatisfying(TagResponse.class, tag -> {
                    assertThat(tag.getName()).isEqualTo("Sample tag");
                });

        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateTagCommand.class));
    }

    @Test
    void list() {
        Mockito.when(tagProvider.lookup())
                .thenReturn(Collections.List(
                        new Tag("Sample"),
                        new Tag("Description")));

        Assertions.assertThat(subject.list())
                .isNotNull()
                .extracting(TagResponse::getName)
                .containsExactlyInAnyOrder("Sample", "Description");
    }

    @Test
    void autoCompleteTag() {
        Mockito.when(tagProvider.lookup(Mockito.any(TagProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(new Tag("Sample")));

        subject.autoCompleteTag("samp");

        var mockFilter = filterFactory.tag();
        Mockito.verify(tagProvider).lookup(Mockito.any(TagProvider.FilterCommand.class));
        Mockito.verify(mockFilter).name("samp", false);
    }
}
