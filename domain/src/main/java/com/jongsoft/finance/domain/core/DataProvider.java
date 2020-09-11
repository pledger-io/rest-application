package com.jongsoft.finance.domain.core;

import com.jongsoft.lang.control.Optional;

public interface DataProvider<T> {

    Optional<T> lookup(long id);

    boolean supports(Class<T> supportingClass);

}
