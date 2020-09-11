package com.jongsoft.finance.domain.transaction.events;

import com.jongsoft.finance.core.ApplicationEvent;
import com.jongsoft.finance.domain.user.UserAccount;

import lombok.Getter;

@Getter
public class TagCreatedEvent implements ApplicationEvent {

    private final UserAccount user;
    private final String tag;

    /**
     * Create a new {@code ApplicationEvent}.
     *  @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     * @param user
     * @param tag
     */
    public TagCreatedEvent(Object source, UserAccount user, String tag) {
        this.user = user;
        this.tag = tag;
    }

}
