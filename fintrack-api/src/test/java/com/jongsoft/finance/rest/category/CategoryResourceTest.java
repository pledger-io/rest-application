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
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

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

        Mockito.when(currentUserProvider.currentUser()).thenReturn(ACTIVE_USER);
    }

    @Test
    void list() {
        Mockito.when(categoryProvider.lookup()).thenReturn(Collections.List(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        subject.list().test()
                .assertValueCount(1)
                .assertComplete();
    }

    @Test
    void search() {
        Mockito.when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(ResultPage.of(
                Category.builder()
                        .id(1L)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        var response = subject.search(new CategorySearchRequest(1));

        Assertions.assertThat(response.getInfo().getRecords()).isEqualTo(1);
        Assertions.assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void autocomplete() {
        Mockito.when(categoryProvider.lookup(Mockito.any(CategoryProvider.FilterCommand.class))).thenReturn(
                ResultPage.of(Category.builder()
                        .id(1L)
                        .label("grocery")
                        .user(ACTIVE_USER)
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        subject.autocomplete("gro").test()
                .assertComplete()
                .assertValueCount(1);

        var mockFilter = filterFactory.category();
        Mockito.verify(categoryProvider).lookup(Mockito.any(CategoryProvider.FilterCommand.class));
        Mockito.verify(mockFilter).label("gro", false);
    }

    @Test
    void create() {
        Mockito.when(categoryProvider.lookup("grocery")).thenReturn(
                Maybe.just(Category.builder()
                        .id(1L)
                        .build()));

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        var response = subject.create(request).blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Mockito.verify(eventPublisher).publishEvent(Mockito.any(CreateCategoryCommand.class));
    }

    @Test
    void get() {
        Mockito.when(categoryProvider.lookup(1L)).thenReturn(
                Control.Option(Category.builder()
                        .id(1L)
                        .user(ACTIVE_USER)
                        .label("grocery")
                        .description("For groceries")
                        .lastActivity(LocalDate.of(2019, 1, 2))
                        .build()));

        var response = subject.get(1L).blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Assertions.assertThat(response.getLabel()).isEqualTo("grocery");
        Assertions.assertThat(response.getDescription()).isEqualTo("For groceries");
        Assertions.assertThat(response.getLastUsed()).isEqualTo(LocalDate.of(2019, 1, 2));
    }

    @Test
    void get_notFound() {
        Mockito.when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        subject.get(1L).test()
                .assertError(StatusException.class)
                .assertErrorMessage("No category found with id 1");
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

        Mockito.when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        var response = subject.update(1L, request).blockingGet();

        Assertions.assertThat(response.getId()).isEqualTo(1L);
        Assertions.assertThat(response.getLabel()).isEqualTo("grocery");
        Assertions.assertThat(response.getDescription()).isEqualTo("Sample");
        Assertions.assertThat(response.getLastUsed()).isEqualTo(LocalDate.of(2019, 1, 2));

        Mockito.verify(category).rename("grocery", "Sample");
    }

    @Test
    void update_notFound() {
        Mockito.when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        subject.update(1L, request).test()
                .assertError(StatusException.class)
                .assertErrorMessage("No category found with id 1");
    }

    @Test
    void delete() {
        Category category = Mockito.mock(Category.class);

        Mockito.when(category.getUser()).thenReturn(ACTIVE_USER);
        Mockito.when(categoryProvider.lookup(1L)).thenReturn(Control.Option(category));

        subject.delete(1L);

        Mockito.verify(category).remove();
    }
}
