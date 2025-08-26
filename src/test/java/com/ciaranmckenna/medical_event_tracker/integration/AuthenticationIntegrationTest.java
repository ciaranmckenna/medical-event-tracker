package com.ciaranmckenna.medical_event_tracker.integration;

import com.ciaranmckenna.medical_event_tracker.dto.AuthResponse;
import com.ciaranmckenna.medical_event_tracker.dto.LoginRequest;
import com.ciaranmckenna.medical_event_tracker.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCompleteAuthenticationFlow() throws Exception {

        RegisterRequest registerRequest = new RegisterRequest(
            "medicaluser",
            "medicaluser@example.com",
            "Password123!",
            "John",
            "Doe"
        );

        // Test user registration
        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("medicaluser"))
                .andExpect(jsonPath("$.email").value("medicaluser@example.com"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String registerResponseJson = registerResult.getResponse().getContentAsString();
        AuthResponse registerResponse = objectMapper.readValue(registerResponseJson, AuthResponse.class);
        String token = registerResponse.token();
        assertNotNull(token);

        // Test user login
        LoginRequest loginRequest = new LoginRequest("medicaluser", "Password123!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("medicaluser"))
                .andExpect(jsonPath("$.token").exists());

        // Verify token is valid by checking it's not null and has proper format
        assertNotNull(token);
        assertTrue(token.length() > 100); // JWT tokens are typically quite long
        assertTrue(token.contains(".")); // JWT has dots separating header.payload.signature
        
        // Test unauthorized access (no token) to a protected endpoint
        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testUsernameAvailabilityCheck() throws Exception {

        // Check available username
        mockMvc.perform(get("/api/auth/check-username/newuser"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testEmailAvailabilityCheck() throws Exception {

        // Check available email
        mockMvc.perform(get("/api/auth/check-email/new@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}