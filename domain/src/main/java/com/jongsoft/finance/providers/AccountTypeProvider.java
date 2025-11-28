package com.jongsoft.finance.providers;

import com.jongsoft.lang.collection.Sequence;

public interface AccountTypeProvider {

    Sequence<String> lookup(boolean hidden);
}
