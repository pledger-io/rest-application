package com.jongsoft.finance.providers;

import com.jongsoft.finance.domain.insight.AnalyzeJob;

import java.util.Optional;

public interface AnalyzeJobProvider {

    Optional<AnalyzeJob> first();
}
