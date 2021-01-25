package com.imooc.aspect;
/**
 * @author hzc
 * @date 2020-06-26 18:38
 */

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author hzc
 * @date 2020-06-26 18:38
 */
@Aspect
@Component
@Slf4j
public class ServiceLogAspect {


    @Around("execution(* com.imooc.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("===== 开始执行 {}.{} =====",joinPoint.getTarget().getClass(),joinPoint.getSignature().getName());

        long begin = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long end = System.currentTimeMillis();

        long take = end -begin;

        if(take > 3000){
            log.error("===== 执行结束，耗时：{}毫秒 =====",take);
        }else if(take > 2000){
            log.warn("===== 执行结束，耗时：{}毫秒 =====",take);
        }else{
            log.info("===== 执行结束，耗时：{}毫秒 =====",take);
        }
        return proceed;
    }



}
