package io.github.sibu912.snmbank.loans.web;

import com.jayway.jsonpath.JsonPath;
import io.github.sibu912.snmbank.loans.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the REST API with the real service, repositories and PostgreSQL.
 * Each request runs in its own transaction, just like in production,
 * so this also proves the applicant is loaded before the transaction ends.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/clean-loan-tables.sql")
class LoanApplicationApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitReviewAndApproveAnApplication() throws Exception {
        String bearer = "Bearer " + loginAs("thandi");

        // 1. Submit (public, like the loan form - no login needed)
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
        mockMvc.perform(get("/api/applications/" + reference).header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicant.surname").value("Nkosi"))
                .andExpect(jsonPath("$.channel").value("WEB"));

        // 3. It appears in the list of submitted applications
        mockMvc.perform(get("/api/applications").header("Authorization", bearer).param("status", "SUBMITTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].referenceNumber", hasItem(reference)))
                .andExpect(jsonPath("$.content[0].applicantName").value("Mr Thabo Nkosi"));

        // 4. Approving straight away is not allowed
        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED"}
                                """))
                .andExpect(status().isConflict());

        // 5. Review, then approve
        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "UNDER_REVIEW", "note": "Payslips received"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

        mockMvc.perform(patch("/api/applications/" + reference + "/status")
                        .header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status": "APPROVED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // 6. The history has all three steps, oldest first, with the logged-in reviewer's username
        mockMvc.perform(get("/api/applications/" + reference + "/history").header("Authorization", bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].toStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$[0].changedBy").value("system"))
                .andExpect(jsonPath("$[1].toStatus").value("UNDER_REVIEW"))
                .andExpect(jsonPath("$[1].changedBy").value("thandi"))
                .andExpect(jsonPath("$[2].toStatus").value("APPROVED"));
    }

    @Test
    void unknownReferenceReturns404() throws Exception {
        mockMvc.perform(get("/api/applications/SNM-2026-999999").header("Authorization", "Bearer " + loginAs("sipho")))
                .andExpect(status().isNotFound());
    }

    /** Logs in as one of the demo reviewers from V2__create_reviewer_table.sql and returns the JWT. */
    private String loginAs(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "Reviewer#2026"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
