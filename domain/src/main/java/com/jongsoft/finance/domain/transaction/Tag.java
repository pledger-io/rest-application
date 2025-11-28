package com.jongsoft.finance.domain.transaction;

import com.jongsoft.finance.annotation.BusinessMethod;
import com.jongsoft.finance.messaging.commands.transaction.DeleteTagCommand;

import java.util.Objects;

public class Tag {

    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    @BusinessMethod
    public void archive() {
        DeleteTagCommand.tagDeleted(name);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Tag tag) {
            return name.equals(tag.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
