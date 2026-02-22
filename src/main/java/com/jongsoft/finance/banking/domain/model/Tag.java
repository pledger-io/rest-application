package com.jongsoft.finance.banking.domain.model;

import com.jongsoft.finance.banking.domain.commands.CreateTagCommand;
import com.jongsoft.finance.banking.domain.commands.DeleteTagCommand;

import java.util.Objects;

public class Tag {

    private final String name;

    public Tag(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

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

    public static Tag create(String name) {
        CreateTagCommand.tagCreated(name);
        return new Tag(name);
    }
}
