package com.jongsoft.finance.jpa.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripleProjection<K, T, I> {
    private final K first;
    private final T second;
    private final I third;
}
