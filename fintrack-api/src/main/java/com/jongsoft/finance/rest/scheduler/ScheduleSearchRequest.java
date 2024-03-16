package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.lang.Control;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Serdeable.Deserializable
public class ScheduleSearchRequest {

    public ScheduleSearchRequest(List<EntityRef> contracts, List<EntityRef> accounts) {
        this.contracts = contracts;
        this.accounts = accounts;
    }

    @Serdeable.Deserializable
    public record EntityRef(@NotNull long id) {
    }

    private final List<EntityRef> contracts;

    private final List<EntityRef> accounts;

    public List<EntityRef> getAccounts() {
        return Control.Option(accounts)
                .getOrSupply(List::of);
    }

    public List<EntityRef> getContracts() {
        return Control.Option(contracts)
                .getOrSupply(List::of);
    }

}
