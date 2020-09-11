package com.jongsoft.finance.domain.account;

import com.jongsoft.lang.collection.Sequence;

public interface AccountTypeProvider {

    Sequence<String> lookup(boolean hidden);

}
