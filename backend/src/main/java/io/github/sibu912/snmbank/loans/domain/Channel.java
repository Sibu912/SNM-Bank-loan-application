package io.github.sibu912.snmbank.loans.domain;

/**
 * Where an application came from.
 */
public enum Channel {
    /** The loan form or the Angular admin (REST). */
    WEB,
    /** A partner system (SOAP). */
    SOAP
}
