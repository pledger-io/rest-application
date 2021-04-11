package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.finance.Exportable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

public interface ContractProvider extends DataProvider<Contract>, Exportable<Contract> {

    Maybe<Contract> lookup(String name);

    Flowable<Contract> search(String partialName);

    default boolean supports(Class<Contract> supportingClass) {
        return Contract.class.equals(supportingClass);
    }
}
