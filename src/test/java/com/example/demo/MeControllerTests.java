package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MeControllerTests extends BaseIntegrationTest {

    @Test
    void me_requires_auth_401() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void me_returns_current_user_200() throws Exception {
        register("eve@example.com", "secret123");
        String token = loginAndGetToken("eve@example.com", "secret123");

        mockMvc.perform(get("/api/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eve@example.com"));
    }
}
