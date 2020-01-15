package de.adorsys.multibanking.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@Aspect
@Component
public class RestControllerAspectLogging {

    public static final Marker AUDIT_LOG = MarkerFactory.getMarker("AUDIT");

    @Before("@annotation(io.swagger.v3.oas.annotations.Operation)")
    public void logBefore(final JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes())
            .getRequest();

        log.trace(AUDIT_LOG, "Method: [{}]", request.getMethod());
        log.trace(AUDIT_LOG, "Path: [{}]", request.getServletPath());
        this.logRequestBody(joinPoint);
    }

    @AfterReturning(pointcut = "@annotation(io.swagger.v3.oas.annotations.Operation)",
        returning = "retVal")
    public void logAfterAllMethods(Object retVal) {
        log.trace(AUDIT_LOG, "Response: [{}]", retVal.toString());
    }

    private void logRequestBody(JoinPoint thisJoinPoint) {
        MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        Annotation[][] annotationMatrix = methodSignature.getMethod().getParameterAnnotations();
        int index = -1;
        for (Annotation[] annotations : annotationMatrix) {
            index++;
            for (Annotation annotation : annotations) {
                if (!(annotation instanceof RequestBody))
                    continue;
                Object requestBody = thisJoinPoint.getArgs()[index];
                log.trace(AUDIT_LOG, "Body: [{}]", requestBody.toString());
            }
        }
    }


}



