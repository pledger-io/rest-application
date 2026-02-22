package com.jongsoft.finance.contract.domain.commands;

import com.jongsoft.finance.ApplicationEvent;

import java.time.LocalDate;

/**
 * Represents a command to change contract information. This record includes the contract's ID,
 * name, description, start date, and end date. Implements the ApplicationEvent interface to allow
 * publishing events. Provides a static method contractChanged to create an instance of
 * ChangeContractCommand and publish it.
 */
public record ChangeContractCommand(
        long id, String name, String description, LocalDate start, LocalDate end)
        implements ApplicationEvent {

    /**
     * Creates and publishes a change contract command with the given parameters.
     *
     * @param id The unique identifier of the contract.
     * @param name The new name of the contract.
     * @param description The new description of the contract.
     * @param start The new start date of the contract.
     * @param end The new end date of the contract.
     */
    public static void contractChanged(
            long id, String name, String description, LocalDate start, LocalDate end) {
        new ChangeContractCommand(id, name, description, start, end).publish();
    }
}
