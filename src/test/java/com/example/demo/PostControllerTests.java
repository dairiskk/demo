package com.example.demo;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;


class PostControllerTests extends BaseIntegrationTest {

    @Test
    void getAllPosts_requires_auth_403() throws Exception {
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_requires_auth_403() throws Exception {
        mockMvc.perform(post("/api/posts/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "title": "Unauthorized post", "content": "Should fail" }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_and_getPostsByUser_200() throws Exception {
        // Register + login user
        register("alice@example.com", "secret123");
        String token = loginAndGetToken("alice@example.com", "secret123");

        // Create a post for Alice (id = 1 since first registered)
        mockMvc.perform(post("/api/posts/user/1")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        { "title": "Hello World", "content": "My very first post!" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Hello World"))
                .andExpect(jsonPath("$.content").value("My very first post!"));

        // Fetch posts by Alice
        mockMvc.perform(get("/api/posts/user/1")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Hello World"))
                .andExpect(jsonPath("$[0].content").value("My very first post!"));
    }

}
