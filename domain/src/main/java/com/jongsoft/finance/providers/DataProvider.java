package com.jongsoft.finance.providers;

import com.jongsoft.finance.SupportIndicating;
import com.jongsoft.lang.control.Optional;

public interface DataProvider<T> extends SupportIndicating<T> {

    Optional<T> lookup(long id);

}
