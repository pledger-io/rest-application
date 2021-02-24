package com.jongsoft.finance.rest.localization;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.context.MessageSource;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.util.Locale;

@Controller("/api/localization/lang")
@Secured(SecurityRule.IS_ANONYMOUS)
public class LanguageResource {

    private final MessageSource messageSource;

    public LanguageResource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Get("/{language}/{textKey}")
    public LanguageResponse getText(@PathVariable String language, @PathVariable String textKey) {
        var message = messageSource.getMessage(
                textKey,
                MessageSource.MessageContext.of(Locale.forLanguageTag(language)))
                .orElseThrow(() -> StatusException.notFound("No message found for " + textKey));

        return new LanguageResponse(message);
    }

}
