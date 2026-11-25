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
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The real login: reviewer accounts from PostgreSQL, BCrypt password check and signed JWTs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql("/clean-loan-tables.sql")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void aDemoReviewerCanLogIn() throws Exception {
        login("thandi", "Reviewer#2026")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("thandi"))
                .andExpect(jsonPath("$.displayName").value("Thandi Mokoena"))
                .andExpect(jsonPath("$.expiresAt").exists());
    }

    @Test
    void theUsernameIsNotCaseSensitive() throws Exception {
        login("  THANDI ", "Reviewer#2026").andExpect(status().isOk());
    }

    @Test
    void aWrongPasswordIsRejected() throws Exception {
        login("thandi", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Login failed"))
                .andExpect(jsonPath("$.detail").value("Wrong username or password"));
    }

    @Test
    void anUnknownUserGetsTheSameMessage() throws Exception {
        login("nobody", "Reviewer#2026")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Wrong username or password"));
    }

    @Test
    void aBlankLoginIsAValidationError() throws Exception {
        login("", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void meReturnsTheLoggedInReviewer() throws Exception {
        String token = JsonPath.read(
                login("sipho", "Reviewer#2026").andReturn().getResponse().getContentAsString(), "$.token");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("sipho"))
                .andExpect(jsonPath("$.displayName").value("Sipho Ndlovu"));
    }

    @Test
    void requestsWithoutAValidTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/applications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/applications").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void theLoanFormCanStillSubmitWithoutLoggingIn() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestData.submitJson("2001", 5)))
                .andExpect(status().isCreated());
    }

    private ResultActions login(String username, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username": "%s", "password": "%s"}
                        """.formatted(username, password)));
    }
}
