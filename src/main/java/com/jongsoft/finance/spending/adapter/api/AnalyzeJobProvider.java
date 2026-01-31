package com.jongsoft.finance.spending.adapter.api;

import com.jongsoft.finance.spending.domain.model.AnalyzeJob;

import java.util.Optional;

public interface AnalyzeJobProvider {

    Optional<AnalyzeJob> first();
}
