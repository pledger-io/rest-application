package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.lang.Control;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Deserializable
public class ScheduleSearchRequest {

    @Serdeable.Deserializable
    public record EntityRef(long id) {
    }

    private List<EntityRef> contracts;

    private List<EntityRef> accounts;

    public List<EntityRef> getAccounts() {
        return Control.Option(accounts)
                .getOrSupply(List::of);
    }

    public List<EntityRef> getContracts() {
        return Control.Option(contracts)
                .getOrSupply(List::of);
    }

}
