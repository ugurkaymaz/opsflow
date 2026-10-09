package com.opsflow.backend;

import com.opsflow.backend.entity.AppUser;
import com.opsflow.backend.entity.UserRole;
import com.opsflow.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldReturnStandardized401WhenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/operations")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(401)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Authentication is required")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/operations")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void shouldReturnStandardized403WhenUserDeletesOperation()
            throws Exception {

        AppUser user =
                new AppUser();

        user.setId(2L);
        user.setName("Standard User");
        user.setEmail("user@opsflow.com");
        user.setRole(UserRole.USER);

        String token =
                jwtService.generateToken(user);

        mockMvc.perform(
                        delete("/api/operations/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        content()
                                .contentTypeCompatibleWith(
                                        "application/json"
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(403)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Forbidden")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "You do not have permission to access this resource"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/operations/999999"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }
}