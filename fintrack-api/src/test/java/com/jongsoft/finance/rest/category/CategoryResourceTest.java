package com.jongsoft.finance.rest.category;

import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.messaging.commands.category.CreateCategoryCommand;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.security.CurrentUserProvider;
import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import io.micronaut.context.event.ApplicationEventPublisher;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryResourceTest extends TestSetup {

    private CategoryResource subject;

    private FilterFactory filterFactory;
    @Mock
    private CategoryProvider categoryProvider;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private SettingProvider settingProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        filterFactory = generateFilterMock();

        subject = new CategoryResource(
                filterFactory,
                categoryProvider,
                currentUserProvider,
                settingProvider);

        new EventBus(eventPublisher);

        when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
    }

    @Test
    void list() {
        when(categoryProvider.lookup()).thenReturn(Collections.List(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        StepVerifier.create(subject.list())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void search() {
        when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(ResultPage.of(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        var response = subject.search(new CategorySearchRequest(1));

        assertThat(response.getInfo().getRecords()).isEqualTo(1);
        assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void autocomplete() {
        when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(Category.builder()
                        .id(1L)
                        .label("grocery")
                        .user(ACTIVE_USER)
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        StepVerifier.create(subject.autocomplete("gro"))
                .expectNextCount(1)
                .verifyComplete();

        var mockFilter = filterFactory.category();
        verify(categoryProvider).lookup(Mockito.any(CategoryProvider.FilterCommand.class));
        verify(mockFilter).label("gro", false);
    }

    @Test
    void create() {
        when(categoryProvider.lookup("grocery")).thenReturn(
                Mono.just(Category.builder()
                        .id(1L)
                        .build()));

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        StepVerifier.create(subject.create(request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                })
                .verifyComplete();

        verify(eventPublisher).publishEvent(Mockito.any(CreateCategoryCommand.class));
    }

    @Test
    void get() {
        when(categoryProvider.lookup(1L)).thenReturn(
                Control.Option(Category.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        StepVerifier.create(subject.get(1L))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getLabel()).isEqualTo("grocery");
                    assertThat(response.getDescription()).isEqualTo("For groceries");
                    assertThat(response.getLastUsed()).isEqualTo(LocalDate.of(2019, 1, 2));
                })
                .verifyComplete();

    }

    @Test
    void get_notFound() {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        StepVerifier.create(subject.get(1L))
                .expectErrorMessage("No category found with id 1")
                .verify();
    }

    @Test
    void update() {
        Category category = Mockito.spy(Category.builder()
                .id(1L)
                .label("grocery")
                .user(ACTIVE_USER)
                .description("For groceries")
                .lastActivity(LocalDate.of(2019, 1, 2))
                .build());

        when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        StepVerifier.create(subject.update(1L, request))
                .assertNext(response -> {
                    assertThat(response.getId()).isEqualTo(1L);
                    assertThat(response.getLabel()).isEqualTo("grocery");
                    assertThat(response.getDescription()).isEqualTo("Sample");
                    assertThat(response.getLastUsed()).isEqualTo(LocalDate.of(2019, 1, 2));
                })
                .verifyComplete();

        verify(category).rename("grocery", "Sample");
    }

    @Test
    void update_notFound() {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        StepVerifier.create(subject.update(1L, request))
                .expectErrorMessage("No category found with id 1")
                .verify();
    }

    @Test
    void delete() {
        Category category = Mockito.mock(Category.class);

        when(category.getUser()).thenReturn(ACTIVE_USER);
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        subject.delete(1L);

        verify(category).remove();
    }
}
