package kz.bsbnb.usci.brms.rulesingleton;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ConditionExceptionIntercepter
{
    @Around("execution(* org.drools.rule.constraint.MvelConstraint.isAllowed(..))")
    public Object advice(ProceedingJoinPoint pjp) throws Throwable
    {
        try {
            return pjp.proceed();
        } catch (Exception e) {
            System.out.println("Got some nasty exception!");
            return true;
        }
    }
}
