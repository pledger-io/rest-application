package com.jongsoft.finance.extension;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

public class IntegrationTestOrder implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors()
                .sort((a, b) -> {
                    var aPhase = a.getTestClass().getAnnotation(IntegrationTest.class).phase();
                    var bPhase = b.getTestClass().getAnnotation(IntegrationTest.class).phase();
                    return Integer.compare(aPhase, bPhase);
                });
    }
}
