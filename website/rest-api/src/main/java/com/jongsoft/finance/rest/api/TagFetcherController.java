package com.jongsoft.finance.rest.api;

import com.jongsoft.finance.domain.transaction.Tag;
import com.jongsoft.finance.factory.FilterFactory;
import com.jongsoft.finance.providers.SettingProvider;
import com.jongsoft.finance.providers.TagProvider;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class TagFetcherController implements TagFetcherApi {

    private final TagProvider tagProvider;
    private final FilterFactory filterFactory;
    private final SettingProvider settingProvider;
    private final Logger logger;

    TagFetcherController(
            TagProvider tagProvider, FilterFactory filterFactory, SettingProvider settingProvider) {
        this.tagProvider = tagProvider;
        this.filterFactory = filterFactory;
        this.settingProvider = settingProvider;
        this.logger = LoggerFactory.getLogger(TagFetcherController.class);
    }

    @Override
    public List<String> findTagsBy(String name) {
        logger.info("Fetching all tags, with provided name.");

        if (name != null) {
            var filter = filterFactory
                    .tag()
                    .name(name, false)
                    .page(0, settingProvider.getAutocompleteLimit());

            return tagProvider.lookup(filter).content().map(Tag::name).toJava();
        }

        return tagProvider.lookup().map(Tag::name).toJava();
    }
}
