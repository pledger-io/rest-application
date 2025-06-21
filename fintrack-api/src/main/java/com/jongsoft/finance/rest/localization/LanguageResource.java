package com.jongsoft.finance.rest.localization;

import static com.jongsoft.finance.rest.ApiConstants.TAG_SETTINGS_LOCALIZATION;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.context.MessageSource;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Tags(@Tag(name = TAG_SETTINGS_LOCALIZATION))
@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/api/localization/lang")
public class LanguageResource {

  private final MessageSource messageSource;

  public LanguageResource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Get("/{language}")
  @Operation(summary = "Get a localization file", operationId = "getTranslations")
  public Map<String, String> get(@PathVariable String language) throws IOException {
    var pathPart = "en".equals(language) ? "" : "_" + language;

    var messages = getClass().getResourceAsStream("/i18n/messages" + pathPart + ".properties");
    var validation =
        getClass().getResourceAsStream("/i18n/ValidationMessages" + pathPart + ".properties");

    var response = new HashMap<String, String>();
    var textKeys = new Properties();
    textKeys.load(messages);
    textKeys.load(validation);
    textKeys.forEach((key, value) -> response.put(key.toString(), value.toString()));
    return response;
  }

  @Get("/{language}/{textKey}")
  @Operation(summary = "Get single translation", operationId = "getTranslation")
  LanguageResponse getText(@PathVariable String language, @PathVariable String textKey) {
    var message = messageSource
        .getMessage(textKey, MessageSource.MessageContext.of(Locale.forLanguageTag(language)))
        .orElseThrow(() -> StatusException.notFound("No message found for " + textKey));

    return new LanguageResponse(message);
  }
}
