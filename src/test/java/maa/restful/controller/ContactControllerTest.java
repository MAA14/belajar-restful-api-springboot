package maa.restful.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import maa.restful.dummyData.ContactBuilder;
import maa.restful.dummyData.UserAdmin;
import maa.restful.entity.Contact;
import maa.restful.entity.User;
import maa.restful.model.ContactResponse;
import maa.restful.model.CreateContactRequest;
import maa.restful.model.UpdateContactRequest;
import maa.restful.model.WebResponse;
import maa.restful.repository.ContactRepository;
import maa.restful.repository.UserRepository;
import maa.restful.service.ValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserAdmin userAdmin;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactBuilder contactBuilder;

    @BeforeEach
    void setup() throws Exception{
        contactRepository.deleteAll();
        userRepository.deleteAll();
        userAdmin.reset();
        userAdmin.login();
    }

    @Test
    void testCreateContactSuccess() throws Exception{
        User admin = userAdmin.get();

        // Bikin Request
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("Go");
        request.setLastName("Potato");
        request.setEmail("GoPotato@Example.com");
        request.setPhone("123123123");

        // Kirim Request
        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Harusnya gk error
            assertNull(response.getErrors());

            // Id nya gk boleh kosong
            assertNotNull(response.getData().getId());

            // Harusnya datanya sama
            assertEquals(request.getFirstName(),response.getData().getFirstName());
            assertEquals(request.getLastName(),response.getData().getLastName());
            assertEquals(request.getEmail(),response.getData().getEmail());
            assertEquals(request.getPhone(),response.getData().getPhone());
        });
    }

    @Test
    void testCreateContactWithoutToken() throws Exception{
        // Bikin Request
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName("Go");
        request.setLastName("Potato");
        request.setEmail("GoPotato@Example.com");
        request.setPhone("123123123");

        // Kirim Request
        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Harusnya gk error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testCreateContactBadRequest() throws Exception{
        User admin = userAdmin.get();

        // Bikin Request
        CreateContactRequest request = new CreateContactRequest();
        request.setFirstName(""); // First Name is Blank
        request.setLastName("Potato");
        request.setEmail("GoPotato@Example.com");
        request.setPhone("123123123");

        // Kirim Request
        mockMvc.perform(
                post("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Harusnya gk error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testUpdateContactSuccess() throws Exception {
        // Ambil akun admin
        User admin = userAdmin.get();

        // Bikin contact jadi dulu di Database
        Contact ahya = contactBuilder.build(admin,"Ahya","Aulia");

        // Bikin Request Update contact Ahya
        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("Muhammad Ahya");
        request.setLastName("Aulia (Update)");
        request.setEmail("MuhammadAhya@example.com");
        request.setPhone("456456456");

        // Kirim Request
        mockMvc.perform(
                put("/api/contacts/" + ahya.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Gk boleh error
            assertNull(response.getErrors());

            // Datanya harus ke update
            assertNotEquals(ahya.getFirstName(), response.getData().getFirstName());
            assertNotEquals(ahya.getLastName(), response.getData().getLastName());
            assertNotEquals(ahya.getEmail(), response.getData().getEmail());
            assertNotEquals(ahya.getPhone(), response.getData().getPhone());

            // Data update harus benar
            assertEquals("Muhammad Ahya", response.getData().getFirstName());
            assertEquals("Aulia (Update)", response.getData().getLastName());
            assertEquals("MuhammadAhya@example.com", response.getData().getEmail());
            assertEquals("456456456", response.getData().getPhone());
        });
    }

    @Test
    void testUpdateContactOnlyPhone() throws Exception {
        // Ambil akun admin
        User admin = userAdmin.get();

        // Bikin contact jadi dulu di Database
        Contact ahya = contactBuilder.build(admin,"Ahya","Aulia");

        // Bikin Request Update contact Ahya
        UpdateContactRequest request = new UpdateContactRequest();
        request.setPhone("456456456");

        // Kirim Request
        mockMvc.perform(
                put("/api/contacts/" + ahya.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Gk boleh error
            assertNull(response.getErrors());

            // Datanya harus ke update
            assertNotEquals(ahya.getPhone(), response.getData().getPhone());

            // Data update harus benar
            assertEquals("456456456", response.getData().getPhone());

            // Sisa Datanya harus sama seperti sebelumnya
            assertEquals(ahya.getFirstName(), response.getData().getFirstName());
            assertEquals(ahya.getLastName(), response.getData().getLastName());
            assertEquals(ahya.getEmail(), response.getData().getEmail());
        });
    }

    @Test
    void testUpdateContactNotFound() throws Exception {
        // Ambil akun admin
        User admin = userAdmin.get();

        // Bikin Request Update contact Ahya
        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("Muhammad Ahya");
        request.setLastName("Aulia (Update)");
        request.setEmail("MuhammadAhya@example.com");
        request.setPhone("456456456");

        // Kirim Request
        mockMvc.perform(
                put("/api/contacts/" + "1231231231") // contactId gk ada
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Gk boleh error
            assertNotNull(response.getErrors());

            // Gk boleh ada Data yang kekirim
            assertNull(response.getData());
        });
    }

    @Test
    void testUpdateContactWithoutToken() throws Exception {
        // Bikin Request Update contact Ahya
        UpdateContactRequest request = new UpdateContactRequest();
        request.setFirstName("Muhammad Ahya");
        request.setLastName("Aulia (Update)");
        request.setEmail("MuhammadAhya@example.com");
        request.setPhone("456456456");

        // Kirim Request
        mockMvc.perform(
                put("/api/contacts/" + "1231231231") // contactId gk ada
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<ContactResponse>>() {});

            // Gk boleh error
            assertNotNull(response.getErrors());

            // Gk boleh ada Data yang kekirim
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteContactSuccess() throws Exception {
        User admin = userAdmin.get();

        Contact dummyContact = contactBuilder.build(admin,"Dummy", "Contact");

        // Kirim request
        mockMvc.perform(
                delete("/api/contacts/" + dummyContact.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            assertNull(response.getErrors());
            assertNull(response.getPaging());

            // Cocokan data
            assertEquals("OK", response.getData());

            // Harus hilang dari database
            assertFalse(contactRepository.existsById(dummyContact.getId()));
        });
    }

    @Test
    void testDeleteContactNotFound() throws Exception {
        User admin = userAdmin.get();

        Contact dummyContact = contactBuilder.build(admin,"Dummy", "Contact");

        // Kirim request
        mockMvc.perform(
                delete("/api/contacts/" + "invalid-contact-id")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getPaging());
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteContactUnauthorizedd() throws Exception {
        User admin = userAdmin.get();

        Contact dummyContact = contactBuilder.build(admin,"Dummy", "Contact");

        // Kirim request
        mockMvc.perform(
                delete("/api/contacts/" + dummyContact.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN","invalid-token")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getPaging());
            assertNull(response.getData());
        });
    }

    @Test
    void testGetContactSuccess() throws Exception {
        User admin = userAdmin.get();

        Contact dummyContact = contactBuilder.build(admin,"Dummy", "Contact");

        // Kirim request
        mockMvc.perform(
                get("/api/contacts/" + dummyContact.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            assertNull(response.getErrors());
            assertNull(response.getPaging());

            // Cocokan data
            assertEquals(dummyContact.getId(), response.getData().getId());
            assertEquals(dummyContact.getFirstName(), response.getData().getFirstName());
            assertEquals(dummyContact.getLastName(), response.getData().getLastName());
            assertEquals(dummyContact.getEmail(), response.getData().getEmail());
            assertEquals(dummyContact.getPhone(), response.getData().getPhone());
        });
    }

    @Test
    void testGetContactNotFound() throws Exception {
        User admin = userAdmin.get();

        // Kirim request
        mockMvc.perform(
                get("/api/contacts/" + "invalid-contact-id")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Harus kosong datanya
            assertNull(response.getData());
            assertNull(response.getPaging());
        });
    }

    @Test
    void testGetContactWithoutToken() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/" + "invalid-contact-id")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<ContactResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Harus kosong datanya
            assertNull(response.getData());
            assertNull(response.getPaging());
        });
    }

    @Test
    void testSearchNotFound() throws Exception {
        User admin = userAdmin.get();

        // Kirim Request
        mockMvc.perform(
                get("/api/contacts")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk() // Dia meski not found tetep 200
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Gk boleh error, karena meski 0 dia tetep memberi response 200
            assertNull(response.getErrors());

            // Karena tidak ada data didalam database jadi harusnya 0
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(0, response.getPaging().getTotalPage());

            // Sizenya di default 10
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void testSearchSuccess() throws Exception {
        User admin = userAdmin.get();

        // Bikin dummy contact di database
        for (int i = 1; i <= 100; i++) {
            contactBuilder.build(admin,"Muhammad" + i,"Ahya Aulia " + i);
        }

        // Kirim request
        mockMvc.perform(
                get("/api/contacts")
                        .param("name","Muhammad")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh kosong
            assertNull(response.getErrors());

            // Data harus muncul semua 10x10 = 100
            assertEquals(0,response.getPaging().getCurrentPage());
            assertEquals(10,response.getData().size()); // Datanya harus berisi 10
            assertEquals(10, response.getPaging().getTotalPage()); // Harus 10x10 = 100

            assertEquals(10,response.getPaging().getSize()); // Max data per page (default = 10)
        });
    }

    @Test
    void testSearchUsingLastName() throws Exception {
        User admin = userAdmin.get();

        // Bikin dummy contact di database
        for (int i = 1; i <= 100; i++) {
            contactBuilder.build(admin,"Muhammad" + i,"Ahya Aulia " + i);
        }

        // Kirim request
        mockMvc.perform(
                get("/api/contacts")
                        .param("name","Ahya Aulia")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh kosong
            assertNull(response.getErrors());

            // Data harus muncul semua 10x10 = 100
            assertEquals(0,response.getPaging().getCurrentPage());
            assertEquals(10,response.getData().size()); // Datanya harus berisi 10
            assertEquals(10, response.getPaging().getTotalPage()); // Harus 10x10 = 100

            assertEquals(10,response.getPaging().getSize()); // Max data per page (default = 10)
        });
    }

    @Test
    void testSearchUsingEmail() throws Exception {
        User admin = userAdmin.get();

        // Bikin dummy contact di database
        for (int i = 1; i <= 100; i++) {
            contactBuilder.build(admin,"Muhammad" + i,"Ahya Aulia " + i);
        }

        // Kirim request
        mockMvc.perform(
                get("/api/contacts")
                        .param("email","Muhammad") // email = firstName@example.com
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh kosong
            assertNull(response.getErrors());

            // Data harus muncul semua 10x10 = 100
            assertEquals(0,response.getPaging().getCurrentPage());
            assertEquals(10,response.getData().size()); // Datanya harus berisi 10
            assertEquals(10, response.getPaging().getTotalPage()); // Harus 10x10 = 100

            assertEquals(10,response.getPaging().getSize()); // Max data per page (default = 10)
        });
    }

    @Test
    void testSearchUsingPhone() throws Exception {
        User admin = userAdmin.get();

        // Bikin dummy contact di database
        for (int i = 1; i <= 100; i++) {
            contactBuilder.build(admin,"Muhammad" + i,"Ahya Aulia " + i);
        }

        // Kirim request
        mockMvc.perform(
                get("/api/contacts")
                        .param("phone","123123123") // phone = 123123123 | berlaku untuk semua contact
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh kosong
            assertNull(response.getErrors());

            // Data harus muncul semua 10x10 = 100
            assertEquals(0,response.getPaging().getCurrentPage());
            assertEquals(10,response.getData().size()); // Datanya harus berisi 10
            assertEquals(10, response.getPaging().getTotalPage()); // Harus 10x10 = 100

            assertEquals(10,response.getPaging().getSize()); // Max data per page (default = 10)
        });
    }

    @Test
    void testSearchUsingSizeMax20() throws Exception {
        User admin = userAdmin.get();

        // Bikin dummy contact di database
        for (int i = 1; i <= 100; i++) {
            contactBuilder.build(admin,"Muhammad" + i,"Ahya Aulia " + i);
        }

        // Kirim request
        mockMvc.perform(
                get("/api/contacts")
                        .param("name","Muhammad") // phone = 123123123 | berlaku untuk semua contact
                        .param("size","20") // 20 data per page
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<ContactResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh kosong
            assertNull(response.getErrors());

            // Data harus muncul semua 20x5 = 100
            assertEquals(0,response.getPaging().getCurrentPage());
            assertEquals(20,response.getData().size()); // Datanya harus berisi 20
            assertEquals(5, response.getPaging().getTotalPage()); // Harus 20x5 = 100

            assertEquals(20,response.getPaging().getSize()); // Max data per page
        });
    }
}