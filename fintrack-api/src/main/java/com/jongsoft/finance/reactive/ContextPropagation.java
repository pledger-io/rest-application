package com.jongsoft.finance.reactive;

import com.jongsoft.lang.Collections;
import io.reactivex.plugins.RxJavaPlugins;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextPropagation {

    private static final Logger log = LoggerFactory.getLogger(ContextPropagation.class);

    private ContextPropagation() {
        // hidden constructor
    }

    public static void configureContext(ReactiveThreadLocal<?>... rxThreadLocals) {
        log.trace("Setting up propagation context.");
        var toPropagate = Collections.List(rxThreadLocals);

        RxJavaPlugins.setScheduleHandler(runner -> {
            var composition = runner;

            for (var threadContext : toPropagate) {
                composition = threadContext.apply(composition);
            }

            return composition;
        });
    }

    public static void unsetContext() {
        log.trace("Destroying propagation context.");
        if (!RxJavaPlugins.isLockdown()) {
            RxJavaPlugins.setScheduleHandler(null);
        }
    }

}
