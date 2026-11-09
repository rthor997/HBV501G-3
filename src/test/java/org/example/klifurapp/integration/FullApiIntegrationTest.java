package org.example.klifurapp.integration;

import org.example.klifurapp.DTO.requests.CreateUserRequestDTO;
import org.example.klifurapp.entity.Climb;
import org.example.klifurapp.entity.User;
import org.example.klifurapp.repository.ClimbRepository;
import org.example.klifurapp.repository.UserRepository;
import org.example.klifurapp.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FullApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired ClimbRepository climbs;
    @Autowired AuthService auth;

    private String accountJson(String name) {
        return "{\"username\":\"" + name + "\",\"email\":\"" + name + "@example.com\",\"password\":\"StrongPass123!\"}";
    }
    private User createUser(String name) {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername(name);
        dto.setEmail(name + "@example.com");
        dto.setPassword("StrongPass123!");
        return auth.register(dto);
    }
    private User createAdmin(String name) {
        CreateUserRequestDTO dto = new CreateUserRequestDTO();
        dto.setUsername(name);
        dto.setEmail(name + "@example.com");
        dto.setPassword("StrongPass123!");
        return auth.createAdmin(dto);
    }
    private String token(String name) throws Exception {
        String body = mvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + name + "@example.com\",\"password\":\"StrongPass123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        java.util.regex.Matcher match = java.util.regex.Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(body);
        assertTrue(match.find(), "Login response must contain a token");
        return match.group(1);
    }
    private String climbJson(String grade, String date) {
        return "{\"gradingSystem\":\"V_SCALE\",\"difficultyGrade\":\"" + grade +
                "\",\"climbingStyle\":\"BOULDERING\",\"date\":\"" + date + "\"}";
    }
    private Long createClimb(String name, String grade, String date) throws Exception {
        User user = users.findByUsername(name).orElseThrow();
        mvc.perform(post("/api/climbs").header("Authorization", "Bearer " + token(name))
                        .contentType(MediaType.APPLICATION_JSON).content(climbJson(grade, date)))
                .andExpect(status().isCreated());
        return climbs.findByUserId(user.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().filter(c -> c.getDifficultyGrade().equals(grade)).findFirst().orElseThrow().getId();
    }

    @Test void publicRegistrationPersistsUserAndHashesPassword() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson("api_registration")))
                .andExpect(status().isCreated());
        User user = users.findByUsername("api_registration").orElseThrow();
        assertNotEquals("StrongPass123!", user.getPasswordHash());
        assertEquals(org.example.klifurapp.entity.Role.USER, user.getRole());
    }
    @Test void loginReturnsRealValidJwt() throws Exception {
        createUser("api_login");
        String jwt = token("api_login");
        assertEquals(3, jwt.split("\\.").length);
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("api_login@example.com"));
    }
    @Test void protectedEndpointRejectsMissingToken() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }
    @Test void protectedEndpointRejectsInvalidToken() throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
    @Test void createClimbPersistsAuthenticatedOwner() throws Exception {
        User owner = createUser("api_owner");
        Long id = createClimb("api_owner", "V3", "2026-11-01");
        Climb saved = climbs.findById(id).orElseThrow();
        assertEquals(owner.getId(), saved.getUser().getId());
        assertEquals("V3", saved.getDifficultyGrade());
    }
    @Test void otherUserCannotDeleteClimb() throws Exception {
        createUser("api_owner2");
        createUser("api_intruder");
        Long id = createClimb("api_owner2", "V2", "2026-11-01");
        mvc.perform(delete("/api/climbs/{id}", id)
                        .header("Authorization", "Bearer " + token("api_intruder")))
                .andExpect(status().isForbidden());
        assertTrue(climbs.existsById(id));
    }
    @Test void ownerCanDeleteClimb() throws Exception {
        createUser("api_owner3");
        Long id = createClimb("api_owner3", "V4", "2026-11-01");
        mvc.perform(delete("/api/climbs/{id}", id)
                        .header("Authorization", "Bearer " + token("api_owner3")))
                .andExpect(status().isNoContent());
        assertFalse(climbs.existsById(id));
    }

    @Test
    void regularUserCannotUseAdminEndpoint() throws Exception {
        createUser("api_regular");

        mvc.perform(post("/api/users/admin")
                        .header("Authorization",
                                "Bearer " + token("api_regular"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson("api_notadmin")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.detail").value("Access denied"))
                .andExpect(jsonPath("$.instance").value("/api/users/admin"));


        assertTrue(users.findByUsername("api_notadmin").isEmpty());
    }

    @Test void adminCanCreateAnotherAdmin() throws Exception {
        createAdmin("api_superadmin");
        mvc.perform(post("/api/users/admin")
                        .header("Authorization", "Bearer " + token("api_superadmin"))
                        .contentType(MediaType.APPLICATION_JSON).content(accountJson("api_newadmin")))
                .andExpect(status().isCreated());
        assertEquals(org.example.klifurapp.entity.Role.ADMIN,
                users.findByUsername("api_newadmin").orElseThrow().getRole());
    }
    @Test void invalidClimbPayloadReturns400AndDoesNotPersist() throws Exception {
        User owner = createUser("api_invalid");
        mvc.perform(post("/api/climbs")
                        .header("Authorization", "Bearer " + token("api_invalid"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"gradingSystem\":\"V_SCALE\",\"climbingStyle\":\"BOULDERING\",\"date\":\"2026-11-01\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(0, climbs.findByUserId(owner.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
    }
    @Test void paginationReturnsCorrectPageAndRejectsBadSize() throws Exception {
        User owner = createUser("api_pages");
        createClimb("api_pages", "V1", "2026-11-01");
        createClimb("api_pages", "V2", "2026-11-02");
        createClimb("api_pages", "V3", "2026-11-03");
        String jwt = token("api_pages");
        mvc.perform(get("/api/climbs/user/{userId}", owner.getId())
                        .param("page", "0").param("size", "2")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].difficultyGrade").value("V3"));
        mvc.perform(get("/api/climbs/user/{userId}", owner.getId())
                        .param("size", "0").header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest());
    }


    @Test
    void duplicateRegistrationReturns409WithErrorBody() throws Exception {
        createUser("api_duplicate");

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(accountJson("api_duplicate")))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value("Username is already taken"))
                .andExpect(jsonPath("$.instance").value("/api/users"));

        assertEquals(1, users.findAll().stream()
                .filter(u -> u.getUsername().equals("api_duplicate"))
                .count());
    }


    @Test
    void unauthenticatedRequestReturns401WithProblemDetail() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Authentication required"))
                .andExpect(jsonPath("$.instance").value("/api/users/me"));
    }


}
