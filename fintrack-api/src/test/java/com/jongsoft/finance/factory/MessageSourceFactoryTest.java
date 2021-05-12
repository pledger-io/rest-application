package com.jongsoft.finance.factory;

import io.micronaut.context.MessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageSourceFactoryTest {

    @Test
    void messageSource() {
        var messageSource = new MessageSourceFactory().messageSource();

        var message = messageSource.getMessage("common.action.save", MessageSource.MessageContext.DEFAULT);
        Assertions.assertTrue(message.isPresent());
    }

}