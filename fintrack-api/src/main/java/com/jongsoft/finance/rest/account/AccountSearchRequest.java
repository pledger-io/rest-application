package com.jongsoft.finance.rest.account;

import com.jongsoft.lang.API;
import com.jongsoft.lang.collection.Sequence;
import lombok.*;

import java.util.List;

@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountSearchRequest {

    private List<String> accountTypes;
    private int page;
    private String name;

    public Sequence<String> accountTypes() {
        return API.Option(accountTypes)
                .map(API::List)
                .getOrSupply(API::List);
    }

    public int page() {
        return page;
    }

    public String name() {
        return name;
    }
}
