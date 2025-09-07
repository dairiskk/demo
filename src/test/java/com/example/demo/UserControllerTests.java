package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTests extends BaseMvcNoSecurityTest {

    @Test
    void create_user_201() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                  {"email":"bob1@example.com","password":"secret123"}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("bob1@example.com"));
    }

    @Test
    void list_users_200() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                  {"email":"a@example.com","password":"secret123"}
                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    void get_by_id_200_then_404() throws Exception {
        var res = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                  {"email":"g@example.com","password":"secret123"}
                """))
                .andReturn();

        String body = res.getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("g@example.com"));

        mockMvc.perform(get("/api/users/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_put_duplicate_email_conflict_409() throws Exception {
        // u1
        var r1 = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"email":"u1@example.com","password":"secret123"}
                """)).andReturn();
        String id1 = r1.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        // u2
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                  {"email":"u2@example.com","password":"secret123"}
                """))
                .andExpect(status().isCreated());

        // duplicate email on u1
        mockMvc.perform(put("/api/users/{id}", id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                  {"email":"u2@example.com","password":"secret123","firstName":"X","lastName":"Y"}
                """))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_user_204_then_404() throws Exception {
        var res = mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                  {"email":"del@example.com","password":"secret123"}
                """)).andReturn();
        String id = res.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound());
    }
}
