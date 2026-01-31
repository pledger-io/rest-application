package com.jongsoft.finance.banking.adapter.rest;

import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.banking.domain.model.Tag;
import com.jongsoft.finance.core.adapter.api.SettingProvider;
import com.jongsoft.finance.core.domain.FilterProvider;
import com.jongsoft.finance.rest.TagFetcherApi;

import io.micronaut.http.annotation.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
class TagFetcherController implements TagFetcherApi {

    private final TagProvider tagProvider;
    private final FilterProvider<TagProvider.FilterCommand> filterFactory;
    private final SettingProvider settingProvider;
    private final Logger logger;

    TagFetcherController(
            TagProvider tagProvider,
            FilterProvider<TagProvider.FilterCommand> filterFactory,
            SettingProvider settingProvider) {
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
                    .create()
                    .name(name, false)
                    .page(0, settingProvider.getAutocompleteLimit());

            return tagProvider.lookup(filter).content().map(Tag::name).toJava();
        }

        return tagProvider.lookup().map(Tag::name).toJava();
    }
}
