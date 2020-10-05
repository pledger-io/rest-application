package com.jongsoft.finance.bpmn.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.inject.Singleton;
import java.util.Properties;

@Singleton
public class PropertyConversionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Properties converted = new Properties();

        execution.getVariableNamesLocal()
                .stream()
                .filter(n -> execution.getVariableLocal(n) != null)
                .forEach(n -> converted.put(n, execution.getVariableLocal(n)));

        execution.setVariable("propertyConversionResult", converted);
    }

}
