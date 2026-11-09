
package org.example.klifurapp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    static class TestController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("User not found");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new ConflictException("Email already registered");
        }

        @GetMapping("/test/forbidden")
        void forbidden() {
            throw new ForbiddenException("Access denied");
        }

        @GetMapping("/test/bad-request")
        void badRequest() {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid grade"
            );
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("Sensitive internal detail");
        }
    }


    @Test
    void missingResourceReturns404() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found"))
                .andExpect(jsonPath("$.instance").value("/test/not-found"));
    }

    @Test
    void duplicateResourceReturns409() throws Exception {
        mvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void forbiddenReturns403() throws Exception {
        mvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }


    @Test
    void responseStatusExceptionPreserves400() throws Exception {
        mvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Invalid grade"))
                .andExpect(jsonPath("$.instance").value("/test/bad-request"));
    }



    @Test
    void unexpectedExceptionReturnsSafe500() throws Exception {
        mvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail")
                        .value("An unexpected error occurred"))
                .andExpect(jsonPath("$.detail")
                        .value(org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(
                                        "Sensitive internal detail"
                                )
                        )));
    }

    @Test
    void noResourceFoundExceptionReturns404() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/does-not-exist");


        NoResourceFoundException exception =
                new NoResourceFoundException(
                        HttpMethod.GET,
                        "/api/does-not-exist",
                        "api/does-not-exist"
                );

        var response = handler.handleNoResourceFound(exception, request);


        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getTitle());
        assertEquals("Resource not found", response.getBody().getDetail());
        assertEquals(
                URI.create("/api/does-not-exist"),
                response.getBody().getInstance()
        );

    }


}
