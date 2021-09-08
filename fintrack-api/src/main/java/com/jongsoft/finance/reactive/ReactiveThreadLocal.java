package com.jongsoft.finance.reactive;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public record ReactiveThreadLocal<T>(Supplier<T> extractor, Consumer<T> configurer, Consumer<T> restorer)
        implements UnaryOperator<Runnable> {

    @Override
    public Runnable apply(Runnable delegate) {
        var callerContext = extractor.get();
        return () -> {
            var originalContext = extractor.get();
            try {
                configurer.accept(callerContext);
                delegate.run();
            } finally {
                restorer.accept(originalContext);
            }
        };
    }

    /**
     * Creates an {@link ReactiveThreadLocal}.
     *
     * @param <T>        the type of the to-be propagated objects
     * @param extractor  the operation using which the context to be propagated and the context to be
     *                   replaced are extracted before a {@link Runnable} is scheduled by an RxJava.
     * @param configurer the operation that updates the relevant thread local context just before a
     *                   scheduled {@link Runnable} is invoked, based on the previously extracted context
     * @param restorer   the operation that restores the original thread local context following
     *                   completion of the scheduled {@link Runnable}
     */
    public static <T> ReactiveThreadLocal<T> from(
            Supplier<T> extractor,
            Consumer<T> configurer,
            Consumer<T> restorer) {
        return new ReactiveThreadLocal<>(extractor, configurer, restorer);
    }

    /**
     * Creates an {@link ReactiveThreadLocal} with identical set and restore operations.
     *
     * @param <T> the type of the to-be propagated objects
     * @param extractor the operation using which the context to be propagated and the context to be
     *     replaced are extracted before a {@link Runnable} is scheduled by an RxJava
     * @param setAndRestore the operation that updates and restores the relevant thread local context
     *     before and after the execution of a scheduled {@link Runnable}
     * @return
     */
    public static <T> ReactiveThreadLocal<T> from(Supplier<T> extractor, Consumer<T> setAndRestore) {
        return new ReactiveThreadLocal<>(extractor, setAndRestore, setAndRestore);
    }

    public static <T> ReactiveThreadLocal<T> from(ThreadLocal<T> threadLocal) {
        return from(threadLocal::get, threadLocal::set);
    }

}
