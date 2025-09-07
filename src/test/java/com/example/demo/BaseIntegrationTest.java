// src/test/java/com/example/demo/BaseIntegrationTest.java
package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;

    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void beforeEach() {
        // nothing; you can truncate DB here if you want a global clean
    }

    protected void register(String email, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
          {"email":"%s","password":"%s"}
        """.formatted(email, password)))
                .andReturn(); // 201 expected; ignore if already exists
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
        var res = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
          {"email":"%s","password":"%s"}
        """.formatted(email, password)))
                .andReturn();

        String json = res.getResponse().getContentAsString();
        JsonNode node = om.readTree(json);
        return node.get("token").asText();
    }

    protected String bearer(String token) { return "Bearer " + token; }
}
