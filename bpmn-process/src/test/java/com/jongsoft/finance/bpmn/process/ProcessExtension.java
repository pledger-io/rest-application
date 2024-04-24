package com.jongsoft.finance.bpmn.process;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ProcessTestExtension.class})
public @interface ProcessExtension {
}
