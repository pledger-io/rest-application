package com.jongsoft.finance.rest.extension;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@ExtendWith({PledgerTestExtension.class})
public @interface PledgerTest {


}
