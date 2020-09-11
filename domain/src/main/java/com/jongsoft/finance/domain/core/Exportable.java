package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.collection.Sequence;

public interface Exportable<T> {

    Sequence<T> lookup();

}
