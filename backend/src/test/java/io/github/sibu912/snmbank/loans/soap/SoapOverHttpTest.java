package io.github.sibu912.snmbank.loans.soap;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Starts the real web server on a random port and calls it over HTTP,
 * the same way SoapUI or a partner system would.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SoapOverHttpTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void servesTheWsdl() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/ws/loans.wsdl")).GET().build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("LoansPort")
                .contains("SubmitLoanApplication")
                .contains("GetApplicationStatus")
                .contains("http://localhost:" + port + "/ws");
    }

    @Test
    void acceptsASoapEnvelope() throws IOException, InterruptedException {
        String envelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:loan="https://sibu912.github.io/snm-bank/loans/v1">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <loan:SubmitLoanApplicationRequest>
                            <loan:applicant>
                                <loan:title>MRS</loan:title>
                                <loan:givenName>Naledi</loan:givenName>
                                <loan:surname>Mokoena</loan:surname>
                                <loan:gender>FEMALE</loan:gender>
                                <loan:address>
                                    <loan:street>8 Long Street</loan:street>
                                    <loan:suburb>Newtown</loan:suburb>
                                    <loan:province>Gauteng</loan:province>
                                    <loan:postalCode>2001</loan:postalCode>
                                </loan:address>
                            </loan:applicant>
                            <loan:loan>
                                <loan:purpose>INVESTMENT</loan:purpose>
                                <loan:amountBand>LESS_THAN_250K</loan:amountBand>
                                <loan:termYears>10</loan:termYears>
                                <loan:incomeBand>LESS_THAN_50K</loan:incomeBand>
                            </loan:loan>
                        </loan:SubmitLoanApplicationRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
                """;

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/ws"))
                        .header("Content-Type", "text/xml; charset=utf-8")
                        .header("SOAPAction", "\"\"")
                        .POST(HttpRequest.BodyPublishers.ofString(envelope))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body())
                .contains("SubmitLoanApplicationResponse")
                .containsPattern("SNM-\\d{4}-\\d{6}")
                .contains("3514.89");
    }
}
