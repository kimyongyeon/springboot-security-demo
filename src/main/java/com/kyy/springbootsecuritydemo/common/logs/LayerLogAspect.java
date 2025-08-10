package com.kyy.springbootsecuritydemo.common.logs;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect @Component
public class LayerLogAspect {
    private static final Logger log = LoggerFactory.getLogger(LayerLogAspect.class);

    // Controller
    @Around("within(@org.springframework.web.bind.annotation.RestController *) || within(@org.springframework.stereotype.Controller *)")
    public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
        IndentMdc.push("controller");
        try {
            log.info("enter {}", sig(pjp));
            Object ret = pjp.proceed();
            log.info("leave {} -> {}", sig(pjp), shortRet(ret));
            return ret;
        } finally {
            IndentMdc.pop();
        }
    }

    // Service
    @Around("within(@org.springframework.stereotype.Service *)")
    public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {
        IndentMdc.push("service");
        try {
            log.info("enter {}", sig(pjp));
            Object ret = pjp.proceed();
            log.info("leave {} -> {}", sig(pjp), shortRet(ret));
            return ret;
        } finally {
            IndentMdc.pop();
        }
    }

    private String sig(ProceedingJoinPoint pjp) {
        return pjp.getSignature().toShortString();
    }
    private Object shortRet(Object ret) {
        if (ret == null) return "null";
        String s = String.valueOf(ret);
        return s.length() > 120 ? s.substring(0, 120) + "...(truncated)" : s;
    }
}
