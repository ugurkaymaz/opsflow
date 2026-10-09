package com.opsflow.backend;

import com.opsflow.backend.dto.ApiErrorResponse;
import com.opsflow.backend.entity.AppUser;
import com.opsflow.backend.entity.UserRole;
import com.opsflow.backend.exception.GlobalExceptionHandler;
import com.opsflow.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private String token;

    @BeforeEach
    void setUp() {

        AppUser user =
                new AppUser();

        user.setId(2L);
        user.setName("Standard User");
        user.setEmail("user@opsflow.com");
        user.setRole(UserRole.USER);

        token =
                jwtService.generateToken(user);
    }

    @Test
    void shouldReturnBadRequestForInvalidDateParameter()
            throws Exception {

        mockMvc.perform(
                        get("/api/reports/summary")
                                .param(
                                        "startDate",
                                        "invalid-date"
                                )
                                .param(
                                        "endDate",
                                        "2026-10-06"
                                )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Invalid value for parameter: startDate"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/reports/summary"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void shouldReturnBadRequestForMalformedJson()
            throws Exception {

        String malformedJson =
                """
                {
                    "title": "Malformed JSON",
                    "description": "Invalid request body",
                """;

        mockMvc.perform(
                        post("/api/operations")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        malformedJson
                                )
                )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Request body is invalid or malformed"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/operations"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidEmail()
            throws Exception {

        String requestBody =
                """
                {
                    "email": "missing@opsflow.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
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
                                .value(
                                        "Invalid email or password"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/auth/login"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidPassword()
            throws Exception {

        String requestBody =
                """
                {
                    "email": "user@opsflow.com",
                    "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        requestBody
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
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
                                .value(
                                        "Invalid email or password"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/auth/login"
                                )
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        RuntimeException exception =
                new RuntimeException(
                        "Sensitive internal database failure"
                );

        ResponseEntity<ApiErrorResponse> response =
                globalExceptionHandler.handleUnexpectedException(
                        exception,
                        request
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                500,
                response.getBody().getStatus()
        );

        assertEquals(
                "Internal Server Error",
                response.getBody().getError()
        );

        assertEquals(
                "An unexpected error occurred",
                response.getBody().getMessage()
        );

        assertEquals(
                "/api/test",
                response.getBody().getPath()
        );

        assertNotNull(
                response.getBody().getTimestamp()
        );
    }

    @Test
    void shouldReturnNotFoundForUnknownApiRoute()
            throws Exception {

        mockMvc.perform(
                        get("/api/does-not-exist")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(
                        status().isNotFound()
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(404)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Resource not found")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/does-not-exist")
                )
                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                );
    }
}