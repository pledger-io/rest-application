package com.jongsoft.finance.rest.statistic;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable.Serializable
public class BalancePartitionResponse {

  private final String partition;
  private final double balance;

  public BalancePartitionResponse(String partition, double balance) {
    this.partition = partition;
    this.balance = balance;
  }

  public String getPartition() {
    return partition;
  }

  public double getBalance() {
    return balance;
  }
}
