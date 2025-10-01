package com.jongsoft.finance.bpmn;

public class KnownProcesses {

    private KnownProcesses() {
        // private constructor
    }

    /**
     * The process identifier for the process that monitors for contract ending and sends the user a
     * notification before hand.
     */
    public static final String CONTRACT_WARN_EXPIRY = "ContractEndWarning";

    /** The process identifier for the process that handles any type of scheduling. */
    public static final String PROCESS_SCHEDULE = "ProcessScheduler";
}
