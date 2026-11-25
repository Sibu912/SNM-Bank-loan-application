package io.github.sibu912.snmbank.loans.soap;

import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;
import io.github.sibu912.snmbank.loans.service.NewApplication;
import io.github.sibu912.snmbank.loans.soap.generated.Address;
import io.github.sibu912.snmbank.loans.soap.generated.AffordabilityResult;
import io.github.sibu912.snmbank.loans.soap.generated.Applicant;
import io.github.sibu912.snmbank.loans.soap.generated.ApplicationStatus;
import io.github.sibu912.snmbank.loans.soap.generated.GetApplicationStatusResponse;
import io.github.sibu912.snmbank.loans.soap.generated.LoanDetails;
import io.github.sibu912.snmbank.loans.soap.generated.SubmitLoanApplicationRequest;
import io.github.sibu912.snmbank.loans.soap.generated.SubmitLoanApplicationResponse;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

/**
 * Converts between the JAXB classes generated from the XSD and our own classes.
 * <p>
 * The generated enums have the same names as ours (Title, ApplicationStatus, ...), so we
 * convert them through their codes: value() gives the XML code, fromValue(...) goes the other way.
 */
final class SoapMapper {

    private static final DatatypeFactory DATATYPE_FACTORY = createDatatypeFactory();

    private SoapMapper() {
    }

    static NewApplication toNewApplication(SubmitLoanApplicationRequest request) {
        Applicant applicant = request.getApplicant();
        Address address = applicant.getAddress();
        LoanDetails loan = request.getLoan();

        return new NewApplication(
                Title.valueOf(applicant.getTitle().value()),
                applicant.getGivenName().trim(),
                applicant.getSurname().trim(),
                Gender.valueOf(applicant.getGender().value()),
                address.getStreet().trim(),
                address.getSuburb().trim(),
                address.getProvince().trim(),
                address.getPostalCode(),
                LoanPurpose.valueOf(loan.getPurpose().value()),
                LoanAmountBand.valueOf(loan.getAmountBand().value()),
                loan.getTermYears(),
                IncomeBand.valueOf(loan.getIncomeBand().value()),
                Channel.SOAP);
    }

    static SubmitLoanApplicationResponse toSubmitResponse(LoanApplication application) {
        SubmitLoanApplicationResponse response = new SubmitLoanApplicationResponse();
        response.setReferenceNumber(application.getReferenceNumber());
        response.setStatus(ApplicationStatus.fromValue(application.getStatus().name()));
        response.setAffordabilityResult(AffordabilityResult.fromValue(application.getAffordabilityResult().name()));
        response.setEstimatedMonthlyRepayment(application.getEstimatedMonthlyRepayment());
        return response;
    }

    static GetApplicationStatusResponse toStatusResponse(LoanApplication application) {
        GetApplicationStatusResponse response = new GetApplicationStatusResponse();
        response.setReferenceNumber(application.getReferenceNumber());
        response.setStatus(ApplicationStatus.fromValue(application.getStatus().name()));
        response.setLastUpdated(toXmlDateTime(application.getUpdatedAt()));
        return response;
    }

    private static XMLGregorianCalendar toXmlDateTime(LocalDateTime dateTime) {
        GregorianCalendar calendar = GregorianCalendar.from(dateTime.atZone(ZoneId.systemDefault()));
        return DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
    }

    private static DatatypeFactory createDatatypeFactory() {
        try {
            return DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Could not create an XML DatatypeFactory", e);
        }
    }
}
