package com.epam.rd.autocode.spring.project.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("within(@org.springframework.stereotype.Controller *) || within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        return logAround(pjp, "CTRL");
    }

    @Around("within(@org.springframework.stereotype.Service *)")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        return logAround(pjp, "SRV ");
    }

    private Object logAround(ProceedingJoinPoint pjp, String tag) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String classMethod = sig.getDeclaringType().getSimpleName() + "." + sig.getName();

        if (log.isDebugEnabled()) {
            log.debug("[{}] -> {} args={}", tag, classMethod, safeArgs(pjp.getArgs()));
        } else {
            log.info("[{}] -> {}", tag, classMethod);
        }

        try {
            Object result = pjp.proceed();
            long took = System.currentTimeMillis() - start;

            if (log.isDebugEnabled()) {
                log.debug("[{}] <- {} took={}ms result={}", tag, classMethod, took, truncate(String.valueOf(result)));
            } else {
                log.info("[{}] <- {} took={}ms", tag, classMethod, took);
            }
            return result;
        } catch (Throwable ex) {
            long took = System.currentTimeMillis() - start;
            log.error("[{}] !! {} failed after {}ms: {}", tag, classMethod, took, ex.toString(), ex);
            throw ex;
        }
    }

    private String truncate(String s) { return s.length() > 500 ? s.substring(0, 500) + "…": s; }

    private String safeArgs(Object[] args) {
        return Arrays.toString(Arrays.stream(args).map(a -> {
            if (a == null) return "null";
            String str = a.toString();
            if (str.length() > 300) str = str.substring(0, 300) + "…";
            return str;
        }).toArray());
    }

    @Around("execution(* com.epam.rd.autocode.spring.project..service..*(..)) || execution(* com.epam.rd.autocode.spring.project.security.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[SRV ] <- {} took={}ms", joinPoint.getSignature().toShortString(), System.currentTimeMillis() - start);
            return result;
        } catch (UsernameNotFoundException e) {
            log.warn("[SRV ] User not found during auth: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[SRV ] !! {} failed after {}ms: {}", joinPoint.getSignature().toShortString(), System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }

}
