package com.imooc.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 支付类型校验注解
 *
 * @author hzc
 * @date 2020-09-24 22:56
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PayMethodValidator.class)
public @interface PayMethod {

    String message();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
