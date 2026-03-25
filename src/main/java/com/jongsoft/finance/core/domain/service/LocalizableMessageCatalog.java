package com.jongsoft.finance.core.domain.service;

import com.jongsoft.finance.core.adapter.api.LocalizationCatalog;

import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
class LocalizableMessageCatalog implements LocalizationCatalog {

    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    private final ConcurrentHashMap<String, Properties> cache = new ConcurrentHashMap<>();

    public String get(Locale locale, String key) {
        Locale effective = effectiveLocale(locale);
        Properties props = bundleFor(effective);
        String value = props.getProperty(key);
        if (value != null) {
            return value;
        }
        if (!effective.getLanguage().equals(DEFAULT_LOCALE.getLanguage())) {
            value = bundleFor(DEFAULT_LOCALE).getProperty(key);
        }
        return value != null ? value : key;
    }

    @Override
    public Map<String, String> getCatalog(String localeCode) {
        Locale locale = Locale.forLanguageTag(localeCode);
        Properties props = bundleFor(effectiveLocale(locale));
        Map<String, String> map = new HashMap<>();
        for (String name : props.stringPropertyNames()) {
            map.put(name, props.getProperty(name));
        }
        return map;
    }

    private Properties bundleFor(Locale locale) {
        String cacheKey = resourceSuffix(locale);
        return cache.computeIfAbsent(cacheKey, LocalizableMessageCatalog::loadMerged);
    }

    private static String resourceSuffix(Locale locale) {
        String lang = effectiveLocale(locale).getLanguage();
        return switch (lang) {
            case "de" -> "_de";
            case "nl" -> "_nl";
            default -> "";
        };
    }

    private static Locale effectiveLocale(Locale locale) {
        if (locale == null) {
            return DEFAULT_LOCALE;
        }
        return locale;
    }

    private static Properties loadMerged(String pathSuffix) {
        Properties combined = new Properties();
        loadInto(combined, "/i18n/messages" + pathSuffix + ".properties");
        loadInto(combined, "/i18n/ValidationMessages" + pathSuffix + ".properties");
        return combined;
    }

    private static void loadInto(Properties target, String resourcePath) {
        try (InputStream in = LocalizableMessageCatalog.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                Properties chunk = new Properties();
                chunk.load(in);
                target.putAll(chunk);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
