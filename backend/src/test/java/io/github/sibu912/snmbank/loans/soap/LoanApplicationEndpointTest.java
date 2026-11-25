package io.github.sibu912.snmbank.loans.soap;

import io.github.sibu912.snmbank.loans.domain.Channel;
import io.github.sibu912.snmbank.loans.domain.Gender;
import io.github.sibu912.snmbank.loans.domain.IncomeBand;
import io.github.sibu912.snmbank.loans.domain.LoanAmountBand;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.domain.LoanPurpose;
import io.github.sibu912.snmbank.loans.domain.Title;
import io.github.sibu912.snmbank.loans.service.LoanApplicationService;
import io.github.sibu912.snmbank.loans.service.NewApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.clientOrSenderFault;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.validPayload;
import static org.springframework.ws.test.server.ResponseMatchers.xpath;

/**
 * Sends SOAP payloads through Spring-WS (validation, endpoint, service, H2 database)
 * without starting a web server.
 */
@SpringBootTest
@ActiveProfiles("test")
class LoanApplicationEndpointTest {

    private static final Map<String, String> NAMESPACES = Map.of("loan", WebServiceConfig.NAMESPACE);
    private static final ClassPathResource XSD = new ClassPathResource("xsd/loan-application.xsd");

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LoanApplicationService service;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void submitReturnsAReferenceNumber() throws IOException {
        client.sendRequest(withPayload(new StringSource(submitRequest("2001", 5))))
                .andExpect(noFault())
                .andExpect(validPayload(XSD))
                .andExpect(xpath("//loan:referenceNumber", NAMESPACES).exists())
                .andExpect(xpath("//loan:status", NAMESPACES).evaluatesTo("SUBMITTED"))
                .andExpect(xpath("//loan:affordabilityResult", NAMESPACES).evaluatesTo("PASS"))
                .andExpect(xpath("//loan:estimatedMonthlyRepayment", NAMESPACES).evaluatesTo("10996.30"));
    }

    @Test
    void submittedApplicationsAreMarkedAsComingFromSoap() {
        client.sendRequest(withPayload(new StringSource(submitRequest("2001", 5))))
                .andExpect(noFault());

        LoanApplication latest = service.list(null, 0, 1).getContent().getFirst();
        assertThat(latest.getChannel()).isEqualTo(Channel.SOAP);
    }

    @Test
    void anInvalidRequestIsRejectedByTheXsd() {
        // 5 digits for the postal code and a loan term that isn't offered
        client.sendRequest(withPayload(new StringSource(submitRequest("20011", 7))))
                .andExpect(clientOrSenderFault());
    }

    @Test
    void getStatusReturnsTheCurrentStatus() throws IOException {
        LoanApplication application = service.submit(new NewApplication(
                Title.MS, "Lerato", "Dlamini", Gender.FEMALE,
                "5 Main Road", "Rosebank", "Gauteng", "2196",
                LoanPurpose.PROPERTY, LoanAmountBand.FROM_500K_TO_999K, 20, IncomeBand.FROM_50K_TO_99K,
                Channel.WEB));

        client.sendRequest(withPayload(new StringSource(statusRequest(application.getReferenceNumber()))))
                .andExpect(noFault())
                .andExpect(validPayload(XSD))
                .andExpect(xpath("//loan:referenceNumber", NAMESPACES).evaluatesTo(application.getReferenceNumber()))
                .andExpect(xpath("//loan:status", NAMESPACES).evaluatesTo("SUBMITTED"))
                .andExpect(xpath("//loan:lastUpdated", NAMESPACES).exists());
    }

    @Test
    void getStatusOfAnUnknownApplicationReturnsAClientFault() {
        client.sendRequest(withPayload(new StringSource(statusRequest("SNM-2026-999999"))))
                .andExpect(clientOrSenderFault("Application SNM-2026-999999 not found"));
    }

    private static String submitRequest(String postalCode, int termYears) {
        return """
                <loan:SubmitLoanApplicationRequest xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
                    <loan:applicant>
                        <loan:title>MR</loan:title>
                        <loan:givenName>Thabo</loan:givenName>
                        <loan:surname>Nkosi</loan:surname>
                        <loan:gender>MALE</loan:gender>
                        <loan:address>
                            <loan:street>12 Jan Smuts Avenue</loan:street>
                            <loan:suburb>Braamfontein</loan:suburb>
                            <loan:province>Gauteng</loan:province>
                            <loan:postalCode>%s</loan:postalCode>
                        </loan:address>
                    </loan:applicant>
                    <loan:loan>
                        <loan:purpose>VEHICLE</loan:purpose>
                        <loan:amountBand>FROM_250K_TO_499K</loan:amountBand>
                        <loan:termYears>%d</loan:termYears>
                        <loan:incomeBand>FROM_50K_TO_99K</loan:incomeBand>
                    </loan:loan>
                </loan:SubmitLoanApplicationRequest>
                """.formatted(postalCode, termYears);
    }

    private static String statusRequest(String referenceNumber) {
        return """
                <loan:GetApplicationStatusRequest xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
                    <loan:referenceNumber>%s</loan:referenceNumber>
                </loan:GetApplicationStatusRequest>
                """.formatted(referenceNumber);
    }
}
