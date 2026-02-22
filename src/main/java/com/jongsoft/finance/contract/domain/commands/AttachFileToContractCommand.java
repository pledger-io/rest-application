package com.jongsoft.finance.contract.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

public record AttachFileToContractCommand(long id, String fileCode) implements ApplicationEvent {

    /**
     * Attaches a file to a contract.
     *
     * @param id the identifier of the contract to attach the file to
     * @param fileCode the code of the file to attach
     */
    public static void attachFileToContract(long id, String fileCode) {
        new AttachFileToContractCommand(id, fileCode).publish();
    }
}
