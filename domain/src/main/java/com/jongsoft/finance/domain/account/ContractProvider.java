package com.jongsoft.finance.domain.account;

import com.jongsoft.finance.domain.core.DataProvider;
import com.jongsoft.finance.domain.core.Exportable;
import com.jongsoft.lang.control.Optional;
import io.reactivex.Flowable;

public interface ContractProvider extends DataProvider<Contract>, Exportable<Contract> {

    Optional<Contract> lookup(String name);

    Flowable<Contract> search(String partialName);

    default boolean supports(Class<Contract> supportingClass) {
        return Contract.class.equals(supportingClass);
    }
}
