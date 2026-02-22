package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.suggestion.types.RuleColumn;

import java.util.EnumMap;
import java.util.Map;

public class RuleDataSet extends EnumMap<RuleColumn, Object> implements Map<RuleColumn, Object> {

    public RuleDataSet() {
        super(RuleColumn.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T getCasted(RuleColumn key) {
        return (T) super.get(key);
    }
}
