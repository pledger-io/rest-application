package com.jongsoft.finance.rest.statistic;

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
