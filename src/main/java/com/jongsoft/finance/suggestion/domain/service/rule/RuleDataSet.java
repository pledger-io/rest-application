package com.jongsoft.finance.suggestion.domain.service.rule;

import com.jongsoft.finance.suggestion.types.RuleColumn;

import java.util.HashMap;
import java.util.Map;

public class RuleDataSet extends HashMap<RuleColumn, Object> implements Map<RuleColumn, Object> {

    @SuppressWarnings("unchecked")
    public <T> T getCasted(RuleColumn key) {
        return (T) super.get(key);
    }
}
