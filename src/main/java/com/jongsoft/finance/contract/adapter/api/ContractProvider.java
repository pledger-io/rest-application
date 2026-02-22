package com.jongsoft.finance.contract.adapter.api;

import com.jongsoft.finance.contract.domain.model.Contract;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;

public interface ContractProvider {

    Sequence<Contract> lookup();

    Optional<Contract> lookup(long id);

    Optional<Contract> lookup(String name);

    Sequence<Contract> search(String partialName);
}
