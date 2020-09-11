package com.jongsoft.finance.serialized;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.jongsoft.finance.ProcessMapper;
import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleConfigJson implements Serializable {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleJson implements Serializable {
        private String name;
        private String description;
        private boolean restrictive;
        private boolean active;
        private int sort;
        private String group;
        private List<ConditionJson> conditions;
        private List<ChangeJson> changes;

        public static RuleJson fromDomain(TransactionRule rule, BiFunction<RuleColumn, String, String> lookup) {
            return RuleJson.builder()
                    .active(rule.isActive())
                    .restrictive(rule.isRestrictive())
                    .name(rule.getName())
                    .group(rule.getGroup())
                    .changes(rule.getChanges().stream()
                            .map(c -> ChangeJson.builder()
                                    .field(c.getField())
                                    .value(lookup.apply(c.getField(), c.getChange()))
                                    .build())
                            .collect(Collectors.toList()))
                    .conditions(rule.getConditions().stream()
                            .map(c -> ConditionJson.builder()
                                    .field(c.getField())
                                    .operation(c.getOperation())
                                    .value(c.getCondition())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionJson implements Serializable {
        private RuleColumn field;
        private RuleOperation operation;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeJson implements Serializable {
        private RuleColumn field;
        private String value;
    }

    private String slug;
    private List<RuleJson> rules;

    public static RuleConfigJson read(String content) {
        try {
            return ProcessMapper.INSTANCE.readValue(content, RuleConfigJson.class);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid configuration for rules provided.", e);
        }
    }

    public String write() {
        try {
            return ProcessMapper.INSTANCE.writeValueAsString(this);
        } catch (IOException e) {
            throw new IllegalStateException("Invalid configuration provided for rules.");
        }
    }
}
