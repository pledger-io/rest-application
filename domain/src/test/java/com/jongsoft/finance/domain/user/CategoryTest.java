package com.jongsoft.finance.domain.user;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import com.jongsoft.finance.messaging.commands.category.DeleteCategoryCommand;
import com.jongsoft.finance.messaging.commands.category.RenameCategoryCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import com.jongsoft.finance.messaging.EventBus;

import io.micronaut.context.event.ApplicationEventPublisher;

class CategoryTest {

    private Category category;

    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        new EventBus(applicationEventPublisher);

        category = Category.builder()
                .id(1L)
                .description("My long description")
                .label("Sample category")
                .lastActivity(LocalDate.of(2019, 1, 1))
                .build();
    }

    @Test
    void rename() {
        var captor = ArgumentCaptor.forClass(RenameCategoryCommand.class);

        category.rename("New category", "New description");

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(category.getLabel()).isEqualTo("New category");
        assertThat(category.getDescription()).isEqualTo("New description");

        assertThat(captor.getValue().id()).isEqualTo(1L);
        assertThat(captor.getValue().description()).isEqualTo("New description");
        assertThat(captor.getValue().name()).isEqualTo("New category");
    }

    @Test
    void rename_NoChange() {
        category.rename("Sample category", "My long description");
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(Mockito.any());
    }

    @Test
    void remove() {
        var captor = ArgumentCaptor.forClass(DeleteCategoryCommand.class);

        category.remove();

        Mockito.verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(category.getId());
    }
}
