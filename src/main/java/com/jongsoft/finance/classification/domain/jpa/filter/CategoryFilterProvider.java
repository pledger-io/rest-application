package com.jongsoft.finance.classification.domain.jpa.filter;

import com.jongsoft.finance.classification.annotations.ClassificationModuleEnabled;
import com.jongsoft.finance.core.domain.FilterProvider;

import jakarta.inject.Singleton;

@Singleton
@ClassificationModuleEnabled
class CategoryFilterProvider implements FilterProvider<CategoryFilterCommand> {
    @Override
    public CategoryFilterCommand create() {
        return new CategoryFilterCommand();
    }
}
