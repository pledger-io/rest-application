package com.jongsoft.finance.learning.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Factory
class LearningConfiguration {

  @Bean
  @LearningExecutor
  public ExecutorService learningExecutor() {
    return Executors.newScheduledThreadPool(5);
  }
}
