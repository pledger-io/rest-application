package com.jongsoft.finance.bpmn.handler;

import com.jongsoft.finance.annotation.BusinessEventListener;
import com.jongsoft.finance.messaging.CommandHandler;
import com.jongsoft.finance.messaging.commands.StartProcessCommand;
import jakarta.inject.Singleton;
import org.camunda.bpm.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StartProcessHandler implements CommandHandler<StartProcessCommand> {

  private final Logger logger;
  private final ProcessEngine processEngine;

  public StartProcessHandler(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.logger = LoggerFactory.getLogger(StartProcessHandler.class);
  }

  @Override
  @BusinessEventListener
  public void handle(StartProcessCommand command) {
    logger.info("Received start process command for {}.", command.processDefinition());

    processEngine
        .getRuntimeService()
        .startProcessInstanceByKey(command.processDefinition(), command.parameters());
  }
}
