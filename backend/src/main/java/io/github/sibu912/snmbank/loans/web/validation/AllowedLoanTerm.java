package io.github.sibu912.snmbank.loans.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Checks that a loan term is one of the lengths offered on the form.
 * Null is allowed here; use @NotNull as well when the field is required.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedLoanTermValidator.class)
public @interface AllowedLoanTerm {

    String message() default "must be one of 5, 10, 15, 20, 25, 30";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
