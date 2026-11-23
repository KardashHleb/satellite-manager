package com.satellite.app.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    // @Around runs code BEFORE and AFTER the method
    @Around("@annotation(com.satellite.app.AOP.LogExecutionTime)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // Run the method itself
        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        System.out.println(">>> [AOP] Method " + joinPoint.getSignature().getName() +
                " executed in " + executionTime + " ms");

        return proceed;
    }
}