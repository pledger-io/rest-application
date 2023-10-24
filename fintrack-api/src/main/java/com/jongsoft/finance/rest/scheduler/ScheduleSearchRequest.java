package com.jongsoft.finance.rest.scheduler;

import com.jongsoft.lang.Control;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Data;

import java.util.List;

@Serdeable.Deserializable
public class ScheduleSearchRequest {

    @Data
    @Serdeable.Deserializable
    public static class EntityRef {
        private long id;
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
