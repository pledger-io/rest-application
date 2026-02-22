package com.jongsoft.finance.banking.domain.jpa.filter;

import com.jongsoft.finance.banking.adapter.api.TagProvider;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
class TagFilterProvider implements FilterProvider<TagProvider.FilterCommand> {
    @Override
    public TagProvider.FilterCommand create() {
        return new TagFilterCommand();
    }
}
