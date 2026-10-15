package com.czh.common.config;

import com.czh.common.exception.ErrorException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 请求体自动校验切面
 * <p>
 * 目的：Controller 接收 {@code @RequestBody} 时<b>不需要写 {@code @Valid}</b>，
 * 只要 DTO 上有 Jakarta Bean Validation 约束注解，就会在进入方法前自动校验，
 * 失败即抛异常并由 {@code commonExceptionHandler} 统一转为 {@code JSONResult.error(...)}。
 * <p>
 * 规则：
 * <ol>
 *     <li>只校验标注了 {@code @RequestBody} 的参数（即请求体）；</li>
 *     <li>参数上若已显式标注 {@code @Valid} / {@code @Validated}，<b>跳过</b>，
 *         交给 Spring MVC 原生流程（配合方法内 {@code BindingResult} + {@code ValidateUtil.checkError}），避免重复校验；</li>
 *     <li>取第一条约束违规的 message 抛出，与 {@code ValidateUtil.checkError} 行为一致；</li>
 *     <li>支持 DTO 内部的 {@code @Valid} 级联校验。</li>
 * </ol>
 * 放在 common 模块，{@code admin} / {@code api} 共用（两个启动类 {@code @ComponentScan} 均覆盖 {@code com.czh}）。
 */
@Aspect
@Component
@Log4j2
public class AutoValidationAspect {

    /**
     * Spring 容器中的校验器（由 hibernate-validator 自动配置提供）
     */
    @Autowired(required = false)
    private Validator springValidator;

    /**
     *  lazily 初始化的校验器：容器没有 Validator Bean 时退化为默认工厂
     */
    private volatile Validator validator;

    /**
     * 拦截所有 Controller（@RestController / @Controller）中的方法
     */
    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) " +
            "|| @within(org.springframework.stereotype.Controller)")
    public void controllerMethod() {
    }

    @Around("controllerMethod()")
    public Object autoValidate(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return joinPoint.proceed();
        }
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            Annotation[] annotations = paramAnnotations[i];
            // 只处理请求体参数
            if (!hasAnnotation(annotations, RequestBody.class)) {
                continue;
            }
            // 已显式使用 @Valid / @Validated 的交给 Spring MVC 原生校验，避免重复
            if (hasAnnotation(annotations, Valid.class) || hasAnnotation(annotations, Validated.class)) {
                continue;
            }
            Set<ConstraintViolation<Object>> violations = validator().validate(arg);
            if (!violations.isEmpty()) {
                String message = violations.iterator().next().getMessage();
                log.warn("参数校验失败: {}#{} -> {}", method.getDeclaringClass().getSimpleName(), method.getName(), message);
                // 必须抛业务异常：约束 message 本身就是给用户看的文案；
                // 抛裸 RuntimeException 会被 commonExceptionHandler 的兜底分支吞掉，对外只返回 traceId
                throw new ErrorException(message);
            }
        }
        return joinPoint.proceed();
    }

    private Validator validator() {
        if (validator == null) {
            synchronized (this) {
                if (validator == null) {
                    validator = springValidator != null
                            ? springValidator
                            : Validation.buildDefaultValidatorFactory().getValidator();
                }
            }
        }
        return validator;
    }

    private boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> type) {
        if (annotations == null) {
            return false;
        }
        for (Annotation annotation : annotations) {
            if (type.equals(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }
}
