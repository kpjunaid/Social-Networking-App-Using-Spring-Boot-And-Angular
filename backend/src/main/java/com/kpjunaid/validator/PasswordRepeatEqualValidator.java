package com.kpjunaid.validator;

import com.kpjunaid.annotation.PasswordRepeatEqual;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.Objects;

public class PasswordRepeatEqualValidator implements ConstraintValidator<PasswordRepeatEqual, Object> {
    private String passwordFieldFirst;
    private String passwordFieldSecond;
    private String message;

    @Override
    public void initialize(PasswordRepeatEqual constraintAnnotation) {
        passwordFieldFirst = constraintAnnotation.passwordFieldFirst();
        passwordFieldSecond = constraintAnnotation.passwordFieldSecond();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object object, ConstraintValidatorContext context) {
        boolean valid = true;

        try {
            final Object firstFieldObj = getFieldObject(object, passwordFieldFirst);
            final Object secondFieldObj = getFieldObject(object, passwordFieldSecond);
            valid = Objects.equals(firstFieldObj, secondFieldObj);
        } catch (Exception ignored) {}

        if (!valid) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(passwordFieldSecond)
                    .addConstraintViolation()
                    .disableDefaultConstraintViolation();
        }

        return valid;
    }

    private Object getFieldObject(Object object, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
}
