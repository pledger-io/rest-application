package com.jongsoft.finance.bpmn.delegate;

import jakarta.inject.Singleton;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;

import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class PropertyConversionDelegate implements JavaDelegate {

    private static final Logger log = getLogger(PropertyConversionDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        log.debug("{}: Converting the provided local properties into a PropertyMap.",
                execution.getCurrentActivityName());

        var converted = new Properties();

        execution.getVariableNamesLocal()
                .stream()
                .filter(n -> execution.getVariableLocal(n) != null)
                .forEach(n -> converted.put(n, execution.getVariableLocal(n)));

        execution.setVariable("propertyConversionResult", converted);
    }

}
