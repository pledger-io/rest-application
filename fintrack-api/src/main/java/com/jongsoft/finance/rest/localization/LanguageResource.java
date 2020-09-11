package com.jongsoft.finance.rest.localization;

import org.springframework.util.StringUtils;

import io.micronaut.context.MessageSource;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api/localization/lang")
@Secured(SecurityRule.IS_ANONYMOUS)
public class LanguageResource {

    private final MessageSource messageSource;

    public LanguageResource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Get("/{language}/{textKey}")
    public LanguageResponse getText(@PathVariable String language, @PathVariable String textKey) {
        return new LanguageResponse(
                messageSource.getMessage(textKey, MessageSource.MessageContext.of(StringUtils.parseLocale(language))).get());
    }

}
