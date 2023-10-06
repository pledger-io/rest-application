package com.jongsoft.finance.core;

import java.io.Serializable;

public interface AggregateBase extends Serializable {

    /**
     * Returns the unique identifier of the aggregate.
     */
    Long getId();

}
