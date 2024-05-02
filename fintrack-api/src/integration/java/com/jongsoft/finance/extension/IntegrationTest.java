package com.jongsoft.finance.extension;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ExtendWith({IntegrationTestExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public @interface IntegrationTest {

    int phase();

}
