package com.jongsoft.finance.providers;

import com.jongsoft.finance.SupportIndicating;
import com.jongsoft.finance.core.JavaBean;
import com.jongsoft.lang.control.Optional;

public interface DataProvider<T> extends SupportIndicating<T>, JavaBean {

  Optional<T> lookup(long id);
}
