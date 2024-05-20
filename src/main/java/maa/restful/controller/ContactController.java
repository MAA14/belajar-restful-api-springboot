package maa.restful.controller;

import maa.restful.entity.User;
import maa.restful.model.*;
import maa.restful.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping(
            path = "/api/contacts",
            consumes = MediaType.APPLICATION_JSON_VALUE, // Request
            produces = MediaType.APPLICATION_JSON_VALUE // Response
    )
    public WebResponse<ContactResponse> create(User user, @RequestBody CreateContactRequest request) {
        // User diambil dari UserArgumentResolver
        ContactResponse response = contactService.create(user,request);
        return WebResponse.<ContactResponse>builder().data(response).build();
    }

    @PutMapping(
            path = "/api/contacts/{contactId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ContactResponse> update(User user, @PathVariable("contactId") String contactId, @RequestBody UpdateContactRequest request) throws Exception {
        // Kita mengambil contactId lewat Path biar nanti si FE nya gampang kalo mau bikin fitur update

        // Masukin contactId ke dalam Request
        request.setId(contactId);

        // Jalankan service
        ContactResponse response = contactService.update(user,request);
        return WebResponse.<ContactResponse>builder().data(response).build();
    }

    @DeleteMapping(
            path = "/api/contacts/{contactId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(User user, @PathVariable("contactId") String contactId) {
        String response = contactService.delete(user, contactId);
        return WebResponse.<String>builder().data(response).build();
    }

    @GetMapping(
            path = "/api/contacts/{contactId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<ContactResponse> get(User user, @PathVariable("contactId") String contactId) {
        ContactResponse response = contactService.get(user, contactId);
        return WebResponse.<ContactResponse>builder().data(response).build();
    }

    @GetMapping(
            path = "/api/contacts",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<ContactResponse>> search(User user,
                                                     @RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "email", required = false) String email,
                                                     @RequestParam(value = "phone", required = false) String phone,
                                                     @RequestParam(value = "page", required = false, defaultValue = "0") Integer page, // current page
                                                     @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) // max data per page
    {
        SearchContactRequest request = SearchContactRequest.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .page(page) // disini tempat atur page
                .size(size)
                .build();

        Page<ContactResponse> contactResponses = contactService.search(user,request);

        return WebResponse.<List<ContactResponse>>builder()
                .data(contactResponses.getContent())
                .paging(PagingResponse.builder()
                        .currentPage(contactResponses.getNumber()) // bingung? lihat ContactService -> methode search() -> cari line dengan comment "setting paging"
                        .totalPage(contactResponses.getTotalPages())
                        .size(contactResponses.getSize())
                        .build())
                .build();
    }
}
