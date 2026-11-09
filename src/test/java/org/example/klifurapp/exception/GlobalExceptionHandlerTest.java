
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
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
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
                .andExpect(jsonPath("$.message").value("Invalid grade"));
    }

    @Test
    void unexpectedExceptionReturnsSafe500() throws Exception {
        mvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred"))
                .andExpect(jsonPath("$.message")
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
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Resource not found", response.getBody().message());
        assertEquals("/api/does-not-exist", response.getBody().path());
    }


}
