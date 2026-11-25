package io.github.sibu912.snmbank.loans.soap;

import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.service.LoanApplicationService;
import io.github.sibu912.snmbank.loans.soap.generated.GetApplicationStatusRequest;
import io.github.sibu912.snmbank.loans.soap.generated.GetApplicationStatusResponse;
import io.github.sibu912.snmbank.loans.soap.generated.SubmitLoanApplicationRequest;
import io.github.sibu912.snmbank.loans.soap.generated.SubmitLoanApplicationResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * SOAP operations for partner systems. Calls the same service as the REST API.
 * Requests have already been checked against the XSD before they reach these methods.
 */
@Endpoint
public class LoanApplicationEndpoint {

    private final LoanApplicationService service;

    public LoanApplicationEndpoint(LoanApplicationService service) {
        this.service = service;
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE, localPart = "SubmitLoanApplicationRequest")
    @ResponsePayload
    public SubmitLoanApplicationResponse submit(@RequestPayload SubmitLoanApplicationRequest request) {
        LoanApplication application = service.submit(SoapMapper.toNewApplication(request));
        return SoapMapper.toSubmitResponse(application);
    }

    @PayloadRoot(namespace = WebServiceConfig.NAMESPACE, localPart = "GetApplicationStatusRequest")
    @ResponsePayload
    public GetApplicationStatusResponse getStatus(@RequestPayload GetApplicationStatusRequest request) {
        LoanApplication application = service.findByReference(request.getReferenceNumber());
        return SoapMapper.toStatusResponse(application);
    }
}
