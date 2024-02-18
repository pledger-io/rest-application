package com.jongsoft.finance.rest.process;

import com.jongsoft.finance.ProcessVariable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Serdeable
@Schema(name = "VariableMap", description = "A map of variables used in tasks.")
public class VariableMap {
    @Serdeable
    @Schema(name = "VariableList", description = "A list of variables wrapped for the task.")
    public record VariableList(List<ProcessVariable> content) implements ProcessVariable {
    }

    @Serdeable
    @Schema(name = "WrappedVariable", description = "A variable wrapped for the task.")
    public record WrappedVariable<T>(T value) implements ProcessVariable {
    }

    @Setter
    @Getter(value = AccessLevel.PACKAGE)
    @Schema(description = "The actual map of all the variables set for the task.")
    private HashMap<String, ProcessVariable> variables = new HashMap<>();

    public <T> T get(String key) {
        return (T) convertFrom(variables.get(key));
    }

    public void put(String key, Object value) {
        variables.put(key, convertTo(value));
    }

    public Set<String> keySet() {
        return variables.keySet();
    }

    private ProcessVariable convertTo(Object value) {
        if (value instanceof Collection list) {
            return new VariableList(list.stream().map(this::convertTo).toList());
        } else if (value instanceof ProcessVariable variable) {
            return variable;
        } else {
            return new WrappedVariable<>(value);
        }
    }

    private Object convertFrom(ProcessVariable value) {
        if (value instanceof VariableList list) {
            return list.content
                    .stream()
                    .map(this::convertFrom)
                    .toList();
        } else if (value instanceof WrappedVariable wrapped) {
            return wrapped.value;
        }

        return value;
    }
}
