package com.jongsoft.finance.suggestion.domain.service.rule.locator;

import com.jongsoft.finance.banking.adapter.api.LinkableProvider;
import com.jongsoft.finance.banking.domain.model.Classifier;
import com.jongsoft.finance.suggestion.domain.service.rule.ChangeLocator;
import com.jongsoft.finance.suggestion.types.RuleColumn;
import com.jongsoft.lang.control.Optional;

import jakarta.inject.Singleton;

import java.util.List;

@Singleton
class RelationLocator implements ChangeLocator {

    private static final List<RuleColumn> SUPPORTED_COLUMNS =
            List.of(RuleColumn.CATEGORY, RuleColumn.BUDGET, RuleColumn.CONTRACT);

    private final List<LinkableProvider<? extends Classifier>> linkableProviders;

    RelationLocator(List<LinkableProvider<? extends Classifier>> linkableProviders) {
        this.linkableProviders = linkableProviders;
    }

    @Override
    public Object locate(RuleColumn column, String change) {
        String classifierType =
                switch (column) {
                    case BUDGET -> "EXPENSE";
                    case CONTRACT, CATEGORY -> column.name();
                    default -> throw new IllegalArgumentException("Unsupported type");
                };

        return linkableProviders.stream()
                .filter(provider -> provider.typeOf().equals(classifierType))
                .findFirst()
                .map(provider -> provider.lookup(Long.parseLong(change)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Classifier::toString)
                .orElseThrow();
    }

    @Override
    public boolean supports(RuleColumn column) {
        return SUPPORTED_COLUMNS.contains(column);
    }
}
