package io.github.sibu912.snmbank.loans.web;

import io.github.sibu912.snmbank.loans.TestData;
import io.github.sibu912.snmbank.loans.domain.AffordabilityResult;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatus;
import io.github.sibu912.snmbank.loans.domain.ApplicationStatusHistory;
import io.github.sibu912.snmbank.loans.domain.LoanApplication;
import io.github.sibu912.snmbank.loans.service.ApplicationNotFoundException;
import io.github.sibu912.snmbank.loans.service.InvalidStatusChangeException;
import io.github.sibu912.snmbank.loans.service.LoanApplicationService;
import io.github.sibu912.snmbank.loans.service.NewApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests only the web layer: JSON in, JSON out, status codes and validation.
 * The service is a Mockito mock, so no database is involved.
 */
@WebMvcTest(LoanApplicationController.class)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanApplicationService service;

    @Test
    void submitReturns201WithTheReferenceNumberAndLocation() throws Exception {
        when(service.submit(any(NewApplication.class))).thenReturn(TestData.loanApplication(AffordabilityResult.PASS));

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.submitJson("2001", 5)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/applications/SNM-2026-000001"))
                .andExpect(jsonPath("$.referenceNumber").value("SNM-2026-000001"))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.affordabilityResult").value("PASS"))
                .andExpect(jsonPath("$.estimatedMonthlyRepayment").value(10996.30))
                .andExpect(jsonPath("$.createdAt").value("2026-11-25T18:30:00"));
    }

    @Test
    void submitWithABadPostalCodeReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.submitJson("20011", 5)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors['address.postalCode']").value("must be exactly 4 digits"));

        verify(service, never()).submit(any());
    }

    @Test
    void submitWithALoanTermNotOnTheFormReturns400() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.submitJson("2001", 7)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.termYears").value("must be one of 5, 10, 15, 20, 25, 30"));
    }

    @Test
    void submitWithAnUnknownCodeReturns400() throws Exception {
        String json = TestData.submitJson("2001", 5).replace("\"MR\"", "\"DR\"");

        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(service, never()).submit(any());
    }

    @Test
    void getReturnsTheApplication() throws Exception {
        when(service.findByReference(TestData.REFERENCE)).thenReturn(TestData.loanApplication(AffordabilityResult.PASS));

        mockMvc.perform(get("/api/applications/SNM-2026-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenceNumber").value("SNM-2026-000001"))
                .andExpect(jsonPath("$.applicant.givenName").value("Thabo"))
                .andExpect(jsonPath("$.applicant.address.postalCode").value("2001"))
                .andExpect(jsonPath("$.termYears").value(5))
                .andExpect(jsonPath("$.channel").value("WEB"));
    }

    @Test
    void getAnUnknownReferenceReturns404() throws Exception {
        when(service.findByReference("SNM-2026-999999"))
                .thenThrow(new ApplicationNotFoundException("SNM-2026-999999"));

        mockMvc.perform(get("/api/applications/SNM-2026-999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Application not found"))
                .andExpect(jsonPath("$.detail").value("Application SNM-2026-999999 not found"));
    }

    @Test
    void listReturnsAPageOfSummaries() throws Exception {
        LoanApplication application = TestData.loanApplication(AffordabilityResult.PASS);
        when(service.list(null, 0, 20)).thenReturn(new PageImpl<>(List.of(application), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].referenceNumber").value("SNM-2026-000001"))
                .andExpect(jsonPath("$.content[0].applicantName").value("Mr Thabo Nkosi"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listPassesTheStatusFilterAndCapsThePageSize() throws Exception {
        when(service.list(ApplicationStatus.SUBMITTED, 0, 100)).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/applications").param("status", "SUBMITTED").param("size", "500"))
                .andExpect(status().isOk());

        verify(service).list(ApplicationStatus.SUBMITTED, 0, 100);
    }

    @Test
    void changeStatusReturnsTheUpdatedApplication() throws Exception {
        LoanApplication application = TestData.loanApplication(AffordabilityResult.PASS);
        application.moveTo(ApplicationStatus.UNDER_REVIEW);
        when(service.changeStatus(TestData.REFERENCE, ApplicationStatus.UNDER_REVIEW, "Payslips received", "reviewer.thandi"))
                .thenReturn(application);

        mockMvc.perform(patch("/api/applications/SNM-2026-000001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "UNDER_REVIEW", "note": "Payslips received", "changedBy": "reviewer.thandi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }

    @Test
    void changeStatusThatIsNotAllowedReturns409() throws Exception {
        when(service.changeStatus(eq(TestData.REFERENCE), eq(ApplicationStatus.APPROVED), any(), eq("reviewer.thandi")))
                .thenThrow(new InvalidStatusChangeException("Cannot change status from SUBMITTED to APPROVED"));

        mockMvc.perform(patch("/api/applications/SNM-2026-000001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "changedBy": "reviewer.thandi"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Invalid status change"))
                .andExpect(jsonPath("$.detail").value("Cannot change status from SUBMITTED to APPROVED"));
    }

    @Test
    void changeStatusWithoutChangedByReturns400() throws Exception {
        mockMvc.perform(patch("/api/applications/SNM-2026-000001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "UNDER_REVIEW"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.changedBy").exists());
    }

    @Test
    void historyReturnsTheChangesOldestFirst() throws Exception {
        LoanApplication application = TestData.loanApplication(AffordabilityResult.PASS);
        when(service.history(TestData.REFERENCE)).thenReturn(List.of(
                new ApplicationStatusHistory(application, null, ApplicationStatus.SUBMITTED, null, "system"),
                new ApplicationStatusHistory(application, ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW,
                        "Payslips received", "reviewer.thandi")));

        mockMvc.perform(get("/api/applications/SNM-2026-000001/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].fromStatus").doesNotExist())
                .andExpect(jsonPath("$[1].fromStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$[1].changedBy").value("reviewer.thandi"));
    }

    @Test
    void corsAllowsTheAngularDevServer() throws Exception {
        mockMvc.perform(options("/api/applications/SNM-2026-000001/status")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void corsRejectsAnUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/applications")
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
