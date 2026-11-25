package io.github.sibu912.snmbank.loans.web.validation;

import io.github.sibu912.snmbank.loans.domain.LoanTerms;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedLoanTermValidator implements ConstraintValidator<AllowedLoanTerm, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == null || LoanTerms.isAllowed(value);
    }
}
