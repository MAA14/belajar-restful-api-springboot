package maa.restful.controller;

// @PENTING Import ini semua penting untuk testing
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import maa.restful.dummyData.UserAdmin;
import maa.restful.entity.User;
import maa.restful.model.RegisterUserRequest;
import maa.restful.model.UpdateUserRequest;
import maa.restful.model.UserResponse;
import maa.restful.model.WebResponse;
import maa.restful.repository.AddressRepository;
import maa.restful.repository.ContactRepository;
import maa.restful.repository.UserRepository;
import maa.restful.security.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// ---------------------------------------------------------------------------------

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private UserAdmin userAdmin;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    // ObjectMapper buat convert data class jadi data JSON
    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setup(){
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll(); // Drop all users setiap kali test jalan

        userAdmin.reset(); // Jangan lupa reset juga adminnya untuk setiap test
    }

    @Test
    void testRegisterSuccess() throws Exception {
        // Bikin data User
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("maa");
        request.setPassword("koumaa");
        request.setName("Ahya");

        // Kirim Data via Post Request
        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll( // Expect
                status().isOk()
        ).andDo(result -> {
            // Ambil Responsenya trus convert yang tadinya JSON jadi data class WebResponse make readValue
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
            });

            // Cocokin apakah responsenya benar
            assertEquals("Ok",response.getData()); // Harusnya kita dapat response data:"Ok"
        });
    }

    @Test
    void testRegisterBadRequest() throws Exception {
        // Bikin data User
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("");
        request.setPassword("");
        request.setName("");

        // Kirim Data via Post Request
        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll( // Expect
                status().isBadRequest()
        ).andDo(result -> {
            // Ambil Responsenya trus convert yang tadinya JSON jadi data class WebResponse make readValue
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
            });

            // Cocokin apakah responsenya benar
            assertNotNull(response.getErrors()); // Harusnya kita dapat error message
        });
    }

    @Test
    void testRegisterDuplicate() throws Exception {
        // Data dummy || Username gk boleh sama
        User user = new User();
        user.setUsername("maa");
        user.setPassword("1234");
        user.setName("Aulia");
        userRepository.save(user);

        // Bikin data User baru
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("maa");
        request.setPassword("koumaa");
        request.setName("Ahya");

        // Kirim Data via Post Request
        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll( // Expect
                status().isBadRequest()
        ).andDo(result -> {
            // Ambil Responsenya trus convert yang tadinya JSON jadi data class WebResponse make readValue
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>(){});

            // Cocokin apakah responsenya benar
            assertNotNull(response.getErrors()); // Harusnya kita dapat error message
        });
    }

    @Test
    void testGetUserInvalidToken() throws Exception {
        // Kirim Request
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN","Invalid Token")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {});

            // Harusnya error
           assertNotNull(response.getErrors());
        });
    }

    @Test
    void testGetUserWithoutToken() throws Exception {
        // Kirim Request
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {});

            // Harusnya error
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testGetUserSuccess() throws Exception {
        // Adminnya login dulu biar tokennya gk kosong
        userAdmin.login();

        // Ambil akun admin
        User admin = userAdmin.get();

        // Kirim Request
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {});

            // Harusnya error
            assertNull(response.getErrors());

            // Harusnya berhasil
            assertEquals(response.getData().getUsername(),admin.getUsername());
            assertEquals(response.getData().getName(),admin.getName());
        });
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        // Login admin dulu, biar ada tokennya
        userAdmin.login();

        // Gunakan akun admin
        User admin = userAdmin.get();

        // Request
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Admin Update");
        request.setPassword("Password Update");

        // Kirim Request
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {});

            // Harusnya gk ada error
            assertNull(response.getErrors());

            assertEquals(response.getData().getName(), "Admin Update");
            assertEquals(response.getData().getUsername(), admin.getUsername());

            // Check apakah passwordnya beneran berubah
            User adminAfterUpdate = userAdmin.getFresh();

            assertTrue(BCrypt.checkpw("Password Update",adminAfterUpdate.getPassword())); // Kalo sama hasilnya True
        });
    }

    @Test
    void testUpdateName() throws Exception {
        // Login admin dulu, biar ada tokennya
        userAdmin.login();

        // Gunakan akun admin
        User admin = userAdmin.get();

        // Request
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Admin Update");

        // Kirim Request
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {});

            // Harusnya gk ada error
            assertNull(response.getErrors());

            assertEquals(response.getData().getName(), "Admin Update");
            assertEquals(response.getData().getUsername(), admin.getUsername());

            // Check apakah passwordnya beneran berubah
            User adminAfterUpdate = userAdmin.getFresh();

            assertEquals(admin.getPassword(),adminAfterUpdate.getPassword()); // Kalo sama hasilnya BCryptnya tetep sama
        });
    }

    @Test
    void testUpdatePassword() throws Exception {
        // Login admin dulu, biar ada tokennya
        userAdmin.login();

        // Gunakan akun admin
        User admin = userAdmin.get();

        // Request
        UpdateUserRequest request = new UpdateUserRequest();
        request.setPassword("Password Update");

        // Kirim Request
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {});

            // Harusnya gk ada error
            assertNull(response.getErrors());

            // Check apakah passwordnya beneran berubah
            User adminAfterUpdate = userAdmin.getFresh();

            assertTrue(BCrypt.checkpw("Password Update",adminAfterUpdate.getPassword())); // Kalo sama hasilnya BCryptnya tetep sama
        });
    }

    @Test
    void testUpdateWithoutToken() throws Exception {
        // Request
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Admin Update");
        request.setPassword("Password Update");

        // Kirim Request
        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {});

            // Harusnya ada error
            assertNotNull(response.getErrors());
        });
    }
}