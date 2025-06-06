package com.jongsoft.finance.rule.locator;

import com.jongsoft.finance.core.RuleColumn;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class NoopLocator implements ChangeLocator {

  @Override
  public Object locate(RuleColumn column, String change) {
    return change;
  }

  @Override
  public boolean supports(RuleColumn column) {
    return RuleColumn.TAGS.equals(column);
  }
}
