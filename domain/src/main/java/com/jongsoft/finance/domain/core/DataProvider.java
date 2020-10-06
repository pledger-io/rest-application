package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.control.Optional;

public interface DataProvider<T> extends SupportIndicating<T> {

    Optional<T> lookup(long id);

}
