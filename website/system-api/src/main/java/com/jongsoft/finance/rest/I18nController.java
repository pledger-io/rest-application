package com.jongsoft.finance.rest;

import com.jongsoft.finance.core.exception.StatusException;
import io.micronaut.http.annotation.Controller;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class I18nController implements I18nApi {

  private final Logger logger = LoggerFactory.getLogger(I18nApi.class);

  @Override
  public Map<String, @NotNull String> getTranslations(String languageCode) {
    logger.info("Get translations for language code {}.", languageCode);

    var pathPart = "en".equals(languageCode) ? "" : "_" + languageCode;

    var response = new HashMap<String, String>();
    loadProperties("/i18n/messages" + pathPart + ".properties")
        .forEach((key, value) -> response.put(key.toString(), value.toString()));
    loadProperties("/i18n/ValidationMessages" + pathPart + ".properties")
        .forEach((key, value) -> response.put(key.toString(), value.toString()));
    return response;
  }

  private Properties loadProperties(String messageFile) {
    try {
      var textKeys = new Properties();
      textKeys.load(getClass().getResourceAsStream(messageFile));
      return textKeys;
    } catch (IOException e) {
      throw StatusException.internalError("Failed to load the localization file.");
    }
  }
}
