package com.jongsoft.finance.banking.domain.jpa.projection;

public class TripleProjection<K, T, I> {
    private final K first;
    private final T second;
    private final I third;

    public TripleProjection(K first, T second, I third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public K getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public I getThird() {
        return third;
    }
}
