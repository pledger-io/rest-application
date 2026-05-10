package com.jongsoft.finance.core.adapter.api;

import com.jongsoft.finance.core.domain.model.PledgerModule;
import com.jongsoft.lang.control.Optional;

public interface ModuleProvider {

    boolean isModuleEnabled(String code);

    Optional<PledgerModule> getModule(String code);
}
