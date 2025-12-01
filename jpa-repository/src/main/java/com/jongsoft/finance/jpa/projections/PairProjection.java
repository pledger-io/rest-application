package com.jongsoft.finance.jpa.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PairProjection<K, T> {
    private final K key;
    private final T value;
}
