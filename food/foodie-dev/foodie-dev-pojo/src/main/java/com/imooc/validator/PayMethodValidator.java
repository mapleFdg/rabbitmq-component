package com.imooc.validator;

import com.imooc.utils.JSONResult;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 支付类型校验
 *
 * @author hzc
 * @date 2020-09-24 22:57
 */
public class PayMethodValidator implements ConstraintValidator<PayMethod, Object> {

    @Override
    public void initialize(PayMethod constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if(value instanceof Integer){
            if ((Integer) value != com.imooc.enums.PayMethod.WEIXIN.type
                    && (Integer) value != com.imooc.enums.PayMethod.ALIPAY.type) {
                return false;
            }else{
                return true;
            }
        }
        return false;
    }
}
