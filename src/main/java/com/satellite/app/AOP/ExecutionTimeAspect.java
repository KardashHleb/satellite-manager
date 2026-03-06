package com.satellite.app.AOP;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect {

    // Аннотация @Around позволяет выполнить код ДО и ПОСЛЕ метода
    @Around("@annotation(com.satellite.app.aspect.LogExecutionTime)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        // Выполняем сам метод
        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        System.out.println(">>> [AOP] Метод " + joinPoint.getSignature().getName() +
                " выполнен за " + executionTime + " мс");

        return proceed;
    }
}