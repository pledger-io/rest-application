package com.jongsoft.finance.rule;

import java.util.HashMap;
import java.util.Map;

import com.jongsoft.finance.core.RuleColumn;

public class RuleDataSet extends HashMap<RuleColumn, Object> implements Map<RuleColumn, Object> {

    @SuppressWarnings("unchecked")
    public <T> T getCasted(RuleColumn key) {
        return (T) super.get(key);
    }

}
