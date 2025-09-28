package com.jongsoft.finance.serialized;

import com.jongsoft.finance.core.RuleColumn;
import com.jongsoft.finance.core.RuleOperation;
import com.jongsoft.finance.domain.transaction.TransactionRule;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.serde.annotation.Serdeable;
import java.io.Serializable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Serdeable
@JsonSchema(
    title = "Transaction Rules",
    description = "Configuration for transaction rules",
    uri = "/rules")
public class RuleConfigJson implements Serializable {

  @Data
  @Builder
  @Serdeable
  @JsonSchema(title = "Transaction Rule", description = "A single transaction rule", uri = "/rule")
  public static class RuleJson implements Serializable {
    private String name;
    private String description;
    private boolean restrictive;
    private boolean active;
    private int sort;
    private String group;
    private List<ConditionJson> conditions;
    private List<ChangeJson> changes;

    public static RuleJson fromDomain(
        TransactionRule rule, BiFunction<RuleColumn, String, String> lookup) {
      return RuleJson.builder()
          .active(rule.isActive())
          .restrictive(rule.isRestrictive())
          .name(rule.getName())
          .group(rule.getGroup())
          .changes(
              rule.getChanges().stream()
                  .map(
                      c ->
                          ChangeJson.builder()
                              .field(c.getField())
                              .value(lookup.apply(c.getField(), c.getChange()))
                              .build())
                  .collect(Collectors.toList()))
          .conditions(
              rule.getConditions().stream()
                  .map(
                      c ->
                          ConditionJson.builder()
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
  @Serdeable
  public static class ConditionJson implements Serializable {
    private RuleColumn field;
    private RuleOperation operation;
    private String value;
  }

  @Data
  @Builder
  @Serdeable
  public static class ChangeJson implements Serializable {
    private RuleColumn field;
    private String value;
  }

  private String slug;
  private List<RuleJson> rules;
}
