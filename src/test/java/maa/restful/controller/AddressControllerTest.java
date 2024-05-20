package maa.restful.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import maa.restful.dummyData.ContactBuilder;
import maa.restful.dummyData.UserAdmin;
import maa.restful.entity.Address;
import maa.restful.entity.Contact;
import maa.restful.entity.User;
import maa.restful.model.*;
import maa.restful.repository.AddressRepository;
import maa.restful.repository.ContactRepository;
import maa.restful.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAdmin userAdmin;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactBuilder contactBuilder;

    private User admin;

    private Contact dummyContact;

    private Address dummyAddress;

    @BeforeEach
    void setup() throws Exception {
        // Reset database
        addressRepository.deleteAll();
        contactRepository.deleteAll();
        userRepository.deleteAll();

        // Reset Dummy Data
        userAdmin.reset();
        userAdmin.login();

        // Selalu bikin Dummy Data
        admin = userAdmin.get();
        dummyContact = contactBuilder.build(admin,"Dummy","Contact");

        // Setup dummy Address
        dummyAddress = new Address();
        dummyAddress.setId(UUID.randomUUID().toString());
        dummyAddress.setContact(dummyContact);
        dummyAddress.setStreet("Jl. Jendral Soedirman");
        dummyAddress.setCity("West Jakarta");
        dummyAddress.setProvince("Jakarta");
        dummyAddress.setCountry("Indonesia");
        dummyAddress.setPostalCode("123456789");
        addressRepository.save(dummyAddress);
    }

    @Test
    void testCreateAddressSuccess() throws Exception {
        // Bikin request
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("West Jakarta");
        request.setProvince("Jakarta");
        request.setCountry("Indonesia");
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                post("/api/contacts/" + dummyContact.getId() + "/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNull(response.getErrors());

            // Cocokin datanya
            assertEquals(request.getStreet(),response.getData().getStreet());
            assertEquals(request.getCity(),response.getData().getCity());
            assertEquals(request.getProvince(),response.getData().getProvince());
            assertEquals(request.getCountry(),response.getData().getCountry());
            assertEquals(request.getPostalCode(),response.getData().getPostalCode());
            assertNotNull(response.getData().getId());

            // Check apakah di database datanya beneran masuk
            assertTrue(addressRepository.existsById(response.getData().getId()));
        });

    }

    @Test
    void testCreateAddressSuccessOnlyCountry() throws Exception {
        // Bikin request
        CreateAddressRequest request = new CreateAddressRequest();
        request.setCountry("Indonesia");

        // kirim request
        mockMvc.perform(
                post("/api/contacts/" + dummyContact.getId() + "/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk() // Harus berhasil karena yang wajib cuman Country
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNull(response.getErrors());

            // Cocokin datanya
            assertEquals(request.getCountry(),response.getData().getCountry());
            assertNotNull(response.getData().getId()); // Id dibuat oleh server

            // Data sisanya harus kosong
            assertNull(response.getData().getStreet());
            assertNull(response.getData().getCity());
            assertNull(response.getData().getProvince());
            assertNull(response.getData().getPostalCode());

            // Check apakah di database datanya beneran masuk
            assertTrue(addressRepository.existsById(response.getData().getId()));
        });

    }

    @Test
    void testCreateAddressBadRequest() throws Exception {
        // Bikin request
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("West Jakarta");
        request.setProvince("Jakarta");
        request.setCountry(""); // Country punya @NotBlank
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                post("/api/contacts/" + dummyContact.getId() + "/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya error
            assertNotNull(response.getErrors());

            // Harusnya kosong datanya
            assertNull(response.getData());
        });

    }

    @Test
    void testCreateAddressUnauthorized() throws Exception {
        // Bikin request
        CreateAddressRequest request = new CreateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("West Jakarta");
        request.setProvince("Jakarta");
        request.setCountry("Indonesia");
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                post("/api/contacts/" + dummyContact.getId() + "/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", "") // empty token
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya error
            assertNotNull(response.getErrors());

            // Harusnya kosong datanya
            assertNull(response.getData());
        });

    }

    @Test
    void testUpdateAddressSuccess() throws Exception {
        // Ceritanya Address dh dibikin dari awal dengan @BeforeEach
        // Bikin request
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("Los Angeles");
        request.setProvince("London");
        request.setCountry("Netherland");
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                put("/api/contacts/" + dummyContact.getId() + "/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNull(response.getErrors());

            // Cocokin datanya apakah berubah
            assertEquals(request.getStreet(),response.getData().getStreet());
            assertEquals(request.getCity(),response.getData().getCity());
            assertEquals(request.getProvince(),response.getData().getProvince());
            assertEquals(request.getCountry(),response.getData().getCountry());
            assertEquals(request.getPostalCode(),response.getData().getPostalCode());
            assertEquals(dummyAddress.getId(),response.getData().getId());
        });
    }

    @Test
    void testUpdateAddressOnlyCountry() throws Exception {
        // Ceritanya Address dh dibikin dari awal dengan @BeforeEach
        // Bikin request
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setCountry("England"); // Yang wajib cuman country

        // kirim request
        mockMvc.perform(
                put("/api/contacts/" + dummyContact.getId() + "/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNull(response.getErrors());

            // Yang berubah harusnya city aja
            assertEquals(request.getCountry(),response.getData().getCountry());

            // Data sisanya harus jadi null/terhapus
            assertNull(response.getData().getStreet());
            assertNull(response.getData().getProvince());
            assertNull(response.getData().getCity());
            assertNull(response.getData().getPostalCode());

            // Id Address tidak boleh terhapus
            assertNotNull(response.getData().getId());
        });
    }

    @Test
    void testUpdateAddressBadRequest() throws Exception {
        // Ceritanya Address dh dibikin dari awal dengan @BeforeEach
        // Bikin request
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("Los Angeles");
        request.setProvince("London");
        request.setCountry(""); // Country itu wajib gk boleh kosong
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                put("/api/contacts/" + dummyContact.getId() + "/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNotNull(response.getErrors());

            assertNull(response.getData());
        });
    }

    @Test
    void testUpdateAddressUnauthorized() throws Exception {
        // Ceritanya Address dh dibikin dari awal dengan @BeforeEach
        // Bikin request
        UpdateAddressRequest request = new UpdateAddressRequest();
        request.setStreet("Jl. Jendral Soedirman");
        request.setCity("Los Angeles");
        request.setProvince("London");
        request.setCountry("Netherland");
        request.setPostalCode("123456789");

        // kirim request
        mockMvc.perform(
                put("/api/contacts/" + dummyContact.getId() + "/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", "invalid-token")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harusnya tidak error
            assertNotNull(response.getErrors());

            assertNull(response.getData());
        });
    }

    @Test
    void testGetAddressSuccess() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Tidak boleh error
            assertNull(response.getErrors());

            // Data harus benar
            assertEquals(dummyAddress.getCity(), response.getData().getCity());
            assertEquals(dummyAddress.getProvince(), response.getData().getProvince());
            assertEquals(dummyAddress.getPostalCode(), response.getData().getPostalCode());
            assertEquals(dummyAddress.getCountry(), response.getData().getCountry());
            assertEquals(dummyAddress.getStreet(), response.getData().getStreet());
            assertEquals(dummyAddress.getId(), response.getData().getId());
        });
    }

    @Test
    void testGetAddressNotFoundContact() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ "invalid-contact-id" +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testGetAddressNotFoundAddress() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses/" + "invalid-address-id")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testGetAddressUnauthorized() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", "invalid-user-token")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteAddressSuccess() throws Exception {
        // Kirim request
        mockMvc.perform(
                delete("/api/contacts/"+ dummyContact.getId() +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus tidak error
            assertNull(response.getErrors());

            // Data harus OK
            assertEquals("OK",response.getData());

            // Address harus hilang dari database
            assertFalse(addressRepository.existsById(dummyAddress.getId()));
        });
    }

    @Test
    void testDeleteAddressNotFoundContact() throws Exception {
        // Kirim request
        mockMvc.perform(
                delete("/api/contacts/"+ "invalid-contact-id" +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteAddressNotFoundAddress() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses/" + "invalid-address-id")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testDeleteAddressUnauthorized() throws Exception {
        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses/" + dummyAddress.getId())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN", "invalid-user-token")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<AddressResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Data harus kosong
            assertNull(response.getData());
        });
    }

    @Test
    void testListAdresseSuccess() throws Exception {
        for (int i = 1; i <= 5; i++){
            // Setup dummy Address
            Address loopAddress = new Address();
            loopAddress.setId(UUID.randomUUID().toString());
            loopAddress.setContact(dummyContact);
            loopAddress.setStreet("Jl. Jendral Soedirman" + i);
            loopAddress.setCity("West Jakarta");
            loopAddress.setProvince("Jakarta");
            loopAddress.setCountry("Indonesia");
            loopAddress.setPostalCode("123456789");
            addressRepository.save(loopAddress);
        } // Total Address harusnya ada 6 karena 5 dari loop dan 1 dari dummyAddress

        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus tidak error
            assertNull(response.getErrors());

            // Harus ada 6 Addresses
            assertEquals(6,  response.getData().size());
        });
    }

    @Test
    void testListAdresseNotFoundContact() throws Exception {
        for (int i = 1; i <= 5; i++){
            // Setup dummy Address
            Address loopAddress = new Address();
            loopAddress.setId(UUID.randomUUID().toString());
            loopAddress.setContact(dummyContact);
            loopAddress.setStreet("Jl. Jendral Soedirman" + i);
            loopAddress.setCity("West Jakarta");
            loopAddress.setProvince("Jakarta");
            loopAddress.setCountry("Indonesia");
            loopAddress.setPostalCode("123456789");
            addressRepository.save(loopAddress);
        } // Total Address harusnya ada 6 karena 5 dari loop dan 1 dari dummyAddress

        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ "invalid-contact-id" +"/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .header("X-API-TOKEN",admin.getToken())
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Harus kosong datanya
            assertNull(response.getData());
        });
    }

    @Test
    void testListAdresseUnauthorized() throws Exception {
        for (int i = 1; i <= 5; i++){
            // Setup dummy Address
            Address loopAddress = new Address();
            loopAddress.setId(UUID.randomUUID().toString());
            loopAddress.setContact(dummyContact);
            loopAddress.setStreet("Jl. Jendral Soedirman" + i);
            loopAddress.setCity("West Jakarta");
            loopAddress.setProvince("Jakarta");
            loopAddress.setCountry("Indonesia");
            loopAddress.setPostalCode("123456789");
            addressRepository.save(loopAddress);
        } // Total Address harusnya ada 6 karena 5 dari loop dan 1 dari dummyAddress

        // Kirim request
        mockMvc.perform(
                get("/api/contacts/"+ dummyContact.getId() +"/addresses")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<List<AddressResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

            // Harus error
            assertNotNull(response.getErrors());

            // Harus kosong datanya
            assertNull(response.getData());
        });
    }
}