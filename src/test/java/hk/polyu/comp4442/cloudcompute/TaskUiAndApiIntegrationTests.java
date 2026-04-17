package hk.polyu.comp4442.cloudcompute;

import com.fasterxml.jackson.databind.ObjectMapper;
import hk.polyu.comp4442.cloudcompute.repository.AppUserRepository;
import hk.polyu.comp4442.cloudcompute.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskUiAndApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void shouldServePublicPagesAndProtectTaskPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cloud Compute Task Manager")));

        mockMvc.perform(get("/login.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/register.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/task.html").accept(MediaType.TEXT_HTML))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login.html"));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterLoginAndCreateReadUpdateDeleteOwnTask() throws Exception {
                String accessToken = registerAndLogin("demo_user", "demo_user@example.com", "password123");

        Map<String, Object> createPayload = new HashMap<>();
        createPayload.put("title", "Prepare demo script");
        createPayload.put("description", "Finalize 12-minute demo flow");
        createPayload.put("status", "TODO");

        String created = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Prepare demo script"))
                .andExpect(jsonPath("$.ownerUsername").value("demo_user"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].status").value("TODO"))
                .andExpect(jsonPath("$[0].ownerUsername").value("demo_user"));

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("title", "Prepare demo script v2");
        updatePayload.put("description", "Rehearse with screenshots");
        updatePayload.put("status", "IN_PROGRESS");

        mockMvc.perform(put("/api/v1/tasks/{id}", id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Prepare demo script v2"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(delete("/api/v1/tasks/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tasks/{id}", id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TASK_NOT_FOUND"));
    }

    @Test
    void shouldRejectInvalidTaskCreationForAuthenticatedUser() throws Exception {
                String accessToken = registerAndLogin("validation_user", "validation_user@example.com", "password123");

        Map<String, Object> payload = new HashMap<>();
        payload.put("title", "");
        payload.put("description", "bad title");
        payload.put("status", "TODO");

        mockMvc.perform(post("/api/v1/tasks")
                                                .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldRotateRefreshTokenAndRevokeOnLogout() throws Exception {
        Map<String, String> tokens = registerAndLoginTokens("refresh_user", "refresh_user@example.com", "password123");
        String accessToken = tokens.get("accessToken");
        String refreshToken = tokens.get("refreshToken");

        Map<String, Object> refreshPayload = new HashMap<>();
        refreshPayload.put("refreshToken", refreshToken);

        String refreshResponse = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String rotatedRefreshToken = objectMapper.readTree(refreshResponse).get("refreshToken").asText();
        org.junit.jupiter.api.Assertions.assertNotEquals(refreshToken, rotatedRefreshToken);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        Map<String, Object> staleRefreshPayload = new HashMap<>();
        staleRefreshPayload.put("refreshToken", rotatedRefreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staleRefreshPayload)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void shouldReturn4xxForTraversalStyleDownloadRequest() throws Exception {
        String accessToken = registerAndLogin("download_user", "download_user@example.com", "password123");

        MvcResult result = mockMvc.perform(get("/api/v1/files/download/../../evil.txt")
                        .header("Authorization", "Bearer " + accessToken))
                .andReturn();

        int statusCode = result.getResponse().getStatus();
        assertTrue(statusCode == 400 || statusCode == 404,
                "Expected traversal request to be rejected with 400/404 but got: " + statusCode);
    }

    @Test
    void shouldRejectUnsafeOrOversizedUploads() throws Exception {
        String accessToken = registerAndLogin("file_guard_user", "file_guard_user@example.com", "password123");

        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "MZ binary".getBytes());

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(exeFile)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnsupportedMediaType());

        byte[] largePayload = new byte[(5 * 1024 * 1024) + 1];
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "big.txt",
                "text/plain",
                largePayload);

        mockMvc.perform(multipart("/api/v1/files/upload")
                        .file(oversizedFile)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isPayloadTooLarge());
    }

        private String registerAndLogin(String username, String email, String password) throws Exception {
                Map<String, String> tokens = registerAndLoginTokens(username, email, password);
                return tokens.get("accessToken");
        }

        private Map<String, String> registerAndLoginTokens(String username, String email, String password) throws Exception {
        Map<String, Object> registerPayload = new HashMap<>();
        registerPayload.put("username", username);
        registerPayload.put("email", email);
        registerPayload.put("password", password);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value(username));

        Map<String, Object> loginPayload = new HashMap<>();
        loginPayload.put("username", username);
        loginPayload.put("password", password);

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", objectMapper.readTree(loginResponse).get("accessToken").asText());
        tokens.put("refreshToken", objectMapper.readTree(loginResponse).get("refreshToken").asText());
        return tokens;
    }
}
