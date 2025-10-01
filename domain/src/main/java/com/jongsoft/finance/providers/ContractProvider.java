package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.account.Contract;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ContractProvider extends DataProvider<Contract>, Exportable<Contract> {

    Optional<Contract> lookup(String name);

    Sequence<Contract> search(String partialName);

    default boolean supports(Class<?> supportingClass) {
        return Contract.class.equals(supportingClass);
    }
}
