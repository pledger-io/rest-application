package com.jongsoft.finance.rest.account;

import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Builder
@Serdeable
public class AccountSearchRequest {

    @NotEmpty
    private List<String> accountTypes;
    @Min(0)
    private int page;
    private String name;

    public Sequence<String> accountTypes() {
        return Control.Option(accountTypes)
                .map(Collections::List)
                .getOrSupply(Collections::List);
    }

    public int page() {
        return page;
    }

    public String name() {
        return name;
    }
}
