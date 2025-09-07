package com.example.demo;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // ✅ disables Security filters
public abstract class BaseMvcNoSecurityTest {
    @Autowired protected MockMvc mockMvc;
}
