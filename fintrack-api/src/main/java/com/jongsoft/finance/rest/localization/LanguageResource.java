package com.jongsoft.finance.rest.localization;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.context.MessageSource;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.util.Locale;

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/localization/lang")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LanguageResource {

    private final MessageSource messageSource;

    @Get("/{language}/{textKey}")
    public LanguageResponse getText(@PathVariable String language, @PathVariable String textKey) {
        var message = messageSource.getMessage(
                textKey,
                MessageSource.MessageContext.of(Locale.forLanguageTag(language)))
                .orElseThrow(() -> StatusException.notFound("No message found for " + textKey));

        return new LanguageResponse(message);
    }

}
