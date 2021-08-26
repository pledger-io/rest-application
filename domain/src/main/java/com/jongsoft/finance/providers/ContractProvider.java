package com.jongsoft.finance.providers;

import com.jongsoft.finance.Exportable;
import com.jongsoft.finance.domain.account.Contract;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContractProvider extends DataProvider<Contract>, Exportable<Contract> {

    Mono<Contract> lookup(String name);

    Flux<Contract> search(String partialName);

    default boolean supports(Class<Contract> supportingClass) {
        return Contract.class.equals(supportingClass);
    }
}
