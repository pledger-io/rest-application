package com.jongsoft.finance.factory;

import com.jongsoft.finance.core.Encoder;
import com.jongsoft.finance.domain.FinTrack;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;

@Factory
public class ApplicationFactory {

  @Context
  public FinTrack createApplicationDomain(Encoder hashingAlgorithm) {
    return new FinTrack(hashingAlgorithm);
  }
}
