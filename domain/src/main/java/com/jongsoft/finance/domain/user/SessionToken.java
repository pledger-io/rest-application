package com.jongsoft.finance.domain.user;

import com.jongsoft.finance.core.AggregateBase;
import com.jongsoft.lang.time.Range;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SessionToken implements AggregateBase {

    private Long id;
    private String token;
    private String description;
    private Range<LocalDateTime> validity;

}
