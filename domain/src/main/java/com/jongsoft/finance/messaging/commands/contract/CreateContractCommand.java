package com.jongsoft.finance.messaging.commands.contract;

import com.jongsoft.finance.messaging.ApplicationEvent;
import java.time.LocalDate;

public record CreateContractCommand(
    long companyId, String name, String description, LocalDate start, LocalDate end)
    implements ApplicationEvent {

  /**
   * Creates a new contract with the provided details and publishes it to the event bus for
   * further processing.
   *
   * @param companyId the identifier of the company for which the contract is being created
   * @param name the name of the contract
   * @param description a description of the contract
   * @param start the start date of the contract
   * @param end the end date of the contract
   */
  public static void contractCreated(
      long companyId, String name, String description, LocalDate start, LocalDate end) {
    new CreateContractCommand(companyId, name, description, start, end).publish();
  }
}
