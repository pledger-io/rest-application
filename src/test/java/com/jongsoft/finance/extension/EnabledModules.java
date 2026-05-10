package com.jongsoft.finance.extension;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EnabledModules {
    String[] value();
}
