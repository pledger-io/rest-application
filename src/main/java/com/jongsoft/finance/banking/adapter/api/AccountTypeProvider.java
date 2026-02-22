package com.jongsoft.finance.banking.adapter.api;

import com.jongsoft.lang.collection.Sequence;

public interface AccountTypeProvider {

    Sequence<String> lookup(boolean hidden);
}
