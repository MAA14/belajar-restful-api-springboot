package maa.restful.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import maa.restful.dummyData.UserAdmin;
import maa.restful.entity.User;
import maa.restful.model.LoginUserRequest;
import maa.restful.model.TokenResponse;
import maa.restful.model.WebResponse;
import maa.restful.repository.UserRepository;
import maa.restful.security.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // ObjectMapper buat convert data class jadi data JSON
    @Autowired
    private ObjectMapper objectMapper;

    // Admin
    @Autowired
    private UserAdmin userAdmin;

    @BeforeEach
    public void setup() {
        // Reset database
        userRepository.deleteAll();

        // Biar this.admin di userAdmin ikutan kereset
        userAdmin.reset();
    }

    @Test
    void testLoginSuccess() throws Exception {
        User admin = userAdmin.get(); // ambil admin

        // Bikin Request
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("admin");
        request.setPassword("admin");

        // Lakukan Post Mapping
        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            // Ubah Response JSON jadi WebResponse<LoginUserResponse>
            WebResponse<TokenResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<TokenResponse>>() {}
            );

            // Jika berhasil harusnya Token dan ExpiredAt nya ke update
            assertNotNull(response.getData().getToken());
            assertNotNull(response.getData().getTokenExpiredAt());

            // Harusnya gk ada error messages
            assertNull(response.getErrors());
        });
    }

    @Test
    void testLoginBadRequest() throws Exception {
        User admin = userAdmin.get();

        // Bikin Request
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("admin palsu");
        request.setPassword("admin");

        // Lakukan Post Mapping
        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            // Ubah Response JSON jadi WebResponse<LoginUserResponse>
            WebResponse<TokenResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<TokenResponse>>() {}
            );

            // Harusnya ada error
            assertNotNull(response.getErrors());

            // Harusnya gk ada data yang kekirim
            assertNull(response.getData());
        });
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        User admin = userAdmin.get();

        // Bikin Request
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("admin");
        request.setPassword("password salah");

        // Lakukan Post Mapping
        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            // Ubah Response JSON jadi WebResponse<LoginUserResponse>
            WebResponse<TokenResponse> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<TokenResponse>>() {}
            );

            // Harusnya ada error
            assertNotNull(response.getErrors());

            // Harusnya gk ada data yang kekirim
            assertNull(response.getData());
        });
    }

    @Test
    void testLogoutSuccess() throws Exception{
        // Login Admin, buat dapetin token
        userAdmin.login();

        // Ambil akun admin
        User admin = userAdmin.get();

        // Lakukan Delete Mapping
        mockMvc.perform(
                delete("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            // Ubah Response JSON jadi WebResponse<LoginUserResponse>
            WebResponse<String> response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    new TypeReference<WebResponse<String>>() {}
            );

            // Harusnya gk ada error
            assertNull(response.getErrors());

            // Harusnya ada data yang kekirim
            assertEquals(response.getData(),"Ok");

            // Check apakah tokennya terhapus atau tidak
            User adminAfterLogout = userAdmin.getFresh();

            assertNull(adminAfterLogout.getToken());
            assertEquals(adminAfterLogout.getTokenExpiredAt(),0L);
        });

    }

}