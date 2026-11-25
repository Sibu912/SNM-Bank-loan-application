package io.github.sibu912.snmbank.loans.service;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * Thrown when no application has the given reference number.
 * REST turns it into a 404 (GlobalExceptionHandler); SOAP turns it into a Client fault
 * because of the @SoapFault annotation.
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(String referenceNumber) {
        super("Application " + referenceNumber + " not found");
    }
}
