package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface LinkableProvider<T extends Classifier> {

    Sequence<T> lookup();

    Optional<T> lookup(long id);

    String typeOf();
}
