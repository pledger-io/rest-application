package com.jongsoft.finance;

import com.jongsoft.lang.collection.Sequence;

public interface Exportable<T> extends SupportIndicating<T> {

  Sequence<T> lookup();
}
