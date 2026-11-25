package io.github.sibu912.snmbank.loans.web;

import com.jayway.jsonpath.JsonPath;
import io.github.sibu912.snmbank.loans.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the REST API with the real service, repositories and an H2 database.
 * Each request runs in its own transaction, just like in production,
 * so this also proves the applicant is loaded before the transaction ends.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanApplicationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitReviewAndApproveAnApplication() throws Exception {
        // 1. Submit
        MvcResult created = mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.submitJson("2001", 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.affordabilityResult").value("PASS"))
                .andExpect(jsonPath("$.estimatedMonthlyRepayment").value(10996.30))
                .andReturn();
        String reference = JsonPath.read(created.getResponse().getContentAsString(), "$.referenceNumber");

        // 2. Read it back
        mockMvc.perform(get("/api/applications/" + reference))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicant.surname").value("Nkosi"))
                .andExpect(jsonPath("$.channel").value("WEB"));

        // 3. It appears in the list of submitted applications
        mockMvc.perform(get("/api/applications").param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].referenceNumber", hasItem(reference)))
                .andExpect(jsonPath("$.content[0].applicantName").value("Mr Thabo Nkosi"));

        // 4. Approving straight away is not allowed
        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "changedBy": "reviewer.thandi"}
                                """))
                .andExpect(status().isConflict());

        // 5. Review, then approve
        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "UNDER_REVIEW", "note": "Payslips received", "changedBy": "reviewer.thandi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED", "changedBy": "reviewer.thandi"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 6. The history has all three steps, oldest first
        mockMvc.perform(get("/api/applications/" + reference + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].changedBy").value("system"))
                .andExpect(jsonPath("$[1].toStatus").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$[2].toStatus").value("APPROVED"));
    }

    @Test
    void unknownReferenceReturns404() throws Exception {
        mockMvc.perform(get("/api/applications/SNM-2026-999999"))
                .andExpect(status().isNotFound());
    }
}
