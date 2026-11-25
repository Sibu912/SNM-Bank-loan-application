package io.github.sibu912.snmbank.loans.service;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String referenceNumber) {
        super("Application " + referenceNumber + " not found");
    }
}
