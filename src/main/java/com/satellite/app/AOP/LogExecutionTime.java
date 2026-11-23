package com.satellite.app.AOP;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Applies to methods only
@Retention(RetentionPolicy.RUNTIME) // Available at runtime
public @interface LogExecutionTime {
}