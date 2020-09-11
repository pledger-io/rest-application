package com.jongsoft.finance.bpmn.delegate;

import java.util.Properties;

import javax.inject.Singleton;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Singleton
public class PropertyConversionDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Properties converted = new Properties();

        execution.getVariableNamesLocal()
                .forEach(n -> converted.put(n, execution.getVariableLocal(n)));

        execution.setVariable("propertyConversionResult", converted);
    }

}
