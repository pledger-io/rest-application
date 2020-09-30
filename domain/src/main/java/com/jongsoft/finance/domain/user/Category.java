package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.annotation.Aggregate;
import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.finance.domain.user.events.CategoryCreatedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRemovedEvent;
import com.jongsoft.finance.domain.user.events.CategoryRenamedEvent;
import com.jongsoft.finance.messaging.EventBus;
import com.jongsoft.lang.API;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Aggregate
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category implements AggregateBase {

    private Long id;
    private String label;
    private String description;

    private LocalDate lastActivity;
    private transient UserAccount user;

    private boolean delete;

    @BusinessMethod
    public Category(UserAccount user, String label) {
        this.user = user;
        this.label = label;
        EventBus.getBus().send(new CategoryCreatedEvent(this, label, description));
    }

    @BusinessMethod
    public void rename(String label, String description) {
        var hasChanged = API.Equal(this.label, label)
                .append(this.description, description)
                .isNotEqual();

        if (hasChanged) {
            this.label = label;
            this.description = description;
            EventBus.getBus().send(new CategoryRenamedEvent(this, id, label, description));
        }
    }

    @BusinessMethod
    public void remove() {
        this.delete = true;
        EventBus.getBus().send(new CategoryRemovedEvent(this, id));
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
