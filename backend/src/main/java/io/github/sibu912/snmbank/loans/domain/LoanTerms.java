package io.github.sibu912.snmbank.loans.domain;

import java.util.Set;

/**
 * The loan lengths (in years) offered on the form.
 */
public final class LoanTerms {

    public static final Set<Integer> ALLOWED_YEARS = Set.of(5, 10, 15, 20, 25, 30);

    private LoanTerms() {
    }

    public static boolean isAllowed(Integer years) {
        return years != null && ALLOWED_YEARS.contains(years);
    }
}
