package com.jongsoft.finance.providers;

import com.jongsoft.finance.core.SettingType;
import com.jongsoft.finance.domain.core.Setting;
import com.jongsoft.lang.collection.Sequence;
import com.jongsoft.lang.control.Optional;
import java.util.function.Function;

public interface SettingProvider {

  Sequence<Setting> lookup();

  Optional<Setting> lookup(String name);

  default int getBudgetAnalysisMonths() {
    return getSetting("AnalysisBudgetMonths", 3, Integer::valueOf, SettingType.NUMBER);
  }

  default int getPageSize() {
    return getSetting("RecordSetPageSize", 20, Integer::valueOf, SettingType.NUMBER);
  }

  default int getAutocompleteLimit() {
    return getSetting("AutocompleteLimit", 10, Integer::valueOf, SettingType.NUMBER);
  }

  default double getMaximumBudgetDeviation() {
    return getSetting("AnalysisBudgetDeviation", 0.25, Double::valueOf, SettingType.NUMBER);
  }

  default boolean registrationClosed() {
    return !getSetting("RegistrationOpen", true, Boolean::valueOf, SettingType.FLAG);
  }

  default <X> X getSetting(
      String setting, X defaultValue, Function<String, X> conversion, SettingType filter) {
    return lookup(setting)
        .filter(s -> s.getType() == filter)
        .map(Setting::getValue)
        .map(conversion)
        .getOrSupply(() -> defaultValue);
  }
}
