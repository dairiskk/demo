package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserApiTests {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @BeforeEach
    void setup() {
        users.deleteAll();
    }

    // --- CREATE ---

    @Test
    void createUser_created201_and_passwordHiddenIfConfigured() throws Exception {
        String body = """
          {
            "firstName": "Alice",
            "lastName": "Doe",
            "email": "alice@example.com",
            "password": "secret123"
          }
        """;

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                // If your entity uses @JsonProperty(WRITE_ONLY) on password, this will pass.
                // If not, remove the next line or change to exists().
                .andExpect(jsonPath("$.password").doesNotExist());

        assertThat(users.count()).isEqualTo(1);
    }

    @Test
    void createUser_duplicateEmail_conflict409() throws Exception {
        users.save(user("Bob","Doe","bob@example.com","x"));

        String body = """
          {
            "firstName": "Bobby",
            "lastName": "Smith",
            "email": "bob@example.com",
            "password": "secret"
          }
        """;

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_missingOrInvalidFields_badRequest400() throws Exception {
        // Adjust to match your @NotBlank/@Email constraints on the entity
        String body = """
          {
            "firstName": "",
            "lastName": "Doe",
            "email": "not-an-email",
            "password": ""
          }
        """;

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // --- READ ---

    @Test
    void getById_found200_and_notFound404() throws Exception {
        User u = users.save(user("Eve","Stone","eve@example.com","p@ssw0rd"));

        mvc.perform(get("/api/users/{id}", u.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("eve@example.com"));

        mvc.perform(get("/api/users/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_all_ok200_returnsArray() throws Exception {
        users.save(user("A","A","a@example.com","p"));
        users.save(user("B","B","b@example.com","p"));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void search_byEmail_ok200_and_404_when_absent() throws Exception {
        users.save(user("Cari","Jones","cj@example.com","p"));

        mvc.perform(get("/api/users/search").param("email", "cj@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Cari"));

        mvc.perform(get("/api/users/search").param("email", "nope@example.com"))
                .andExpect(status().isNotFound());
    }

    // --- UPDATE (PUT) ---
    // Your controller marks PUT with @Valid, so send a FULL valid payload (not partial).

    @Test
    void update_put_fullPayload_ok200() throws Exception {
        User u = users.save(user("Al","Old","al@example.com","pass1234"));

        String body = """
          {
            "firstName": "Al",
            "lastName": "New",
            "email": "al@example.com",
            "password": "pass1234"
          }
        """;

        mvc.perform(put("/api/users/{id}", u.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("New"));
    }

    @Test
    void update_put_changeEmail_toDuplicate_conflict409() throws Exception {
        users.save(user("One","User","one@example.com","p"));
        User target = users.save(user("Two","User","two@example.com","p"));

        String body = """
          {
            "firstName": "Two",
            "lastName": "User",
            "email": "one@example.com",
            "password": "p"
          }
        """;

        mvc.perform(put("/api/users/{id}", target.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void update_put_missingRequiredFields_badRequest400() throws Exception {
        User u = users.save(user("Req","User","req@example.com","password1"));

        // Missing firstName/email/password will trigger 400 due to @Valid + entity constraints
        String body = """
          { "lastName": "Only" }
        """;

        mvc.perform(put("/api/users/{id}", u.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE ---

    @Test
    void delete_noContent204_then_getNotFound404() throws Exception {
        User u = users.save(user("Del","User","del@example.com","p"));

        mvc.perform(delete("/api/users/{id}", u.getId()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/{id}", u.getId()))
                .andExpect(status().isNotFound());
    }

    // --- helper ---

    private static User user(String fn, String ln, String email, String pw) {
        User u = new User();
        u.setFirstName(fn);
        u.setLastName(ln);
        u.setEmail(email);
        u.setPassword(pw);
        return u;
    }
}
