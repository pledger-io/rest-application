package com.jongsoft.finance.rule;

import com.jongsoft.finance.domain.transaction.TransactionRule;

/**
 * The rule engine is responsible for running all rules against the input data set and returning the output data set.
 */
public interface RuleEngine {

    /**
     * Runs all rules against the input data set and returns the output data set.
     *
     * @param input The input data set.
     * @return The output data set.
     */
    RuleDataSet run(RuleDataSet input);

    /**
     * Runs the specified rule against the input data set and returns the output data set.
     *
     * @param input The input data set.
     * @param rule The rule to run.
     * @return The output data set.
     */
    RuleDataSet run(RuleDataSet input, TransactionRule rule);

}
