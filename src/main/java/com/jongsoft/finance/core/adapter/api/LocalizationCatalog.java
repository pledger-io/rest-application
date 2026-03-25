package com.jongsoft.finance.core.adapter.api;

import java.util.Locale;
import java.util.Map;

public interface LocalizationCatalog {
    String get(Locale locale, String key);

    Map<String, String> getCatalog(String language);
}
