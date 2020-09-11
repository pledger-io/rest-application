package com.jongsoft.finance.domain.core;

import com.jongsoft.finance.core.AggregateBase;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"id"})
public class EntityRef implements AggregateBase {

    private final Long id;

    public EntityRef(Long id) {
        this.id = id;
    }

}
