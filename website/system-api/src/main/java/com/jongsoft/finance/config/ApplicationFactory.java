package com.jongsoft.finance.config;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.FinTrack;

import io.micronaut.context.annotation.Factory;

import jakarta.inject.Singleton;

@Factory
public class ApplicationFactory {

    @Singleton
    public FinTrack createApplication(Encoder encoder) {
        return new FinTrack(encoder);
    }
}
