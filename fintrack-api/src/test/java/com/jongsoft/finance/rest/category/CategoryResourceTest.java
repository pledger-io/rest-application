package com.jongsoft.finance.rest.category;

import com.jongsoft.finance.ResultPage;
import com.jongsoft.finance.core.exception.StatusException;
import com.jongsoft.finance.domain.user.Category;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.finance.messaging.commands.category.CreateCategoryCommand;
import com.jongsoft.finance.providers.CategoryProvider;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.rest.TestSetup;
import com.jongsoft.finance.rest.model.CategoryResponse;
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

        Assertions.assertThat(subject.list())
                .hasSize(1)
                .extracting(CategoryResponse::getId)
                .containsExactly(1L);
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

        Assertions.assertThat(subject.autocomplete("gro"))
                .hasSize(1)
                .extracting(CategoryResponse::getId)
                .containsExactly(1L);

        var mockFilter = filterFactory.category();
        verify(categoryProvider).lookup(Mockito.any(CategoryProvider.FilterCommand.class));
        verify(mockFilter).label("gro", false);
    }

    @Test
    void create() {
        when(categoryProvider.lookup("grocery")).thenReturn(
                Control.Option(Category.builder()
                        .id(1L)
                        .build()));

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        Assertions.assertThat(subject.create(request))
                .extracting(CategoryResponse::getId)
                .isEqualTo(1L);

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

        Assertions.assertThat(subject.get(1L))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("label", "grocery")
                .hasFieldOrPropertyWithValue("description", "For groceries")
                .hasFieldOrPropertyWithValue("lastUsed", LocalDate.of(2019, 1, 2));
    }

    @Test
    void get_notFound() {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        Assertions.assertThatThrownBy(() -> subject.get(1L))
                .isInstanceOf(StatusException.class)
                .hasMessage("No category found with id 1");
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

        Assertions.assertThat(subject.update(1L, request))
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("label", "grocery")
                .hasFieldOrPropertyWithValue("description", "Sample")
                .hasFieldOrPropertyWithValue("lastUsed", LocalDate.of(2019, 1, 2));

        verify(category).rename("grocery", "Sample");
    }

    @Test
    void update_notFound() {
        when(categoryProvider.lookup(1L)).thenReturn(Control.Option());

        var request = CategoryCreateRequest.builder()
                .name("grocery")
                .description("Sample")
                .build();

        Assertions.assertThatThrownBy(() -> subject.update(1L, request))
                .isInstanceOf(StatusException.class)
                .hasMessage("No category found with id 1");
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
