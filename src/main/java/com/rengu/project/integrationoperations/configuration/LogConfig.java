package com.rengu.project.integrationoperations.configuration;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogConfig {
    String value() default "";
}
