package com.jongsoft.finance.factory;

import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import io.micronaut.runtime.context.CompositeMessageSource;

import java.util.List;

@Factory
public class MessageSourceFactory {

    @Bean
    public MessageSource messageSource() {
        var messagesBundle = new ResourceBundleMessageSource("i18n.messages");
        var validationBundle = new ResourceBundleMessageSource("i18n.ValidationMessages");

        return new CompositeMessageSource(List.of(messagesBundle, validationBundle));
    }
}
