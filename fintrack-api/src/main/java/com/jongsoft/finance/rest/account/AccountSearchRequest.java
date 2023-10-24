package com.jongsoft.finance.rest.account;

import com.jongsoft.lang.Collections;
import com.jongsoft.lang.Control;
import com.jongsoft.lang.collection.Sequence;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.util.List;

@Setter
@Builder
@NoArgsConstructor
@Serdeable.Deserializable
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountSearchRequest {

    private List<String> accountTypes;
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
