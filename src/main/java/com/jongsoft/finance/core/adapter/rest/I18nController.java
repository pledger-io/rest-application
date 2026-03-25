package com.jongsoft.finance.core.adapter.rest;

import com.jongsoft.finance.core.adapter.api.LocalizationCatalog;
import com.jongsoft.finance.rest.I18nApi;

import io.micronaut.http.annotation.Controller;

import jakarta.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Controller
class I18nController implements I18nApi {

    private final Logger logger = LoggerFactory.getLogger(I18nController.class);
    private final LocalizationCatalog messageCatalog;

    I18nController(LocalizationCatalog messageCatalog) {
        this.messageCatalog = messageCatalog;
    }

    @Override
    public Map<String, @NotNull String> getTranslations(String languageCode) {
        logger.info("Get translations for language code {}.", languageCode);

        return messageCatalog.getCatalog(languageCode);
    }
}
