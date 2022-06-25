package com.kpjunaid.validator;

import com.kpjunaid.annotation.ValidEmail;
import com.kpjunaid.common.AppConstants;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile(AppConstants.EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches() && (4 <= email.length() && email.length() <= 64);
    }
}
