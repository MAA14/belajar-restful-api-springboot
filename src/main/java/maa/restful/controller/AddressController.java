package maa.restful.controller;

import maa.restful.entity.User;
import maa.restful.model.*;
import maa.restful.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping(
            path = "/api/contacts/{contactId}/addresses",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> create(User user,
                                               @PathVariable("contactId") String contactId,
                                               @RequestBody CreateAddressRequest request) throws Exception {
        // Masukan contactId ke dalam request body
        request.setContactId(contactId);

        // Jalankan service
        AddressResponse response = addressService.create(user,request);
        return WebResponse.<AddressResponse>builder().data(response).build();
    }

    @PutMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> update(User user,
                                               @PathVariable("contactId") String contactId,
                                               @PathVariable("addressId") String addressId,
                                               @RequestBody UpdateAddressRequest request)
    {
        // Masukan path variable sebagai requestbody
        request.setContactId(contactId);
        request.setAddressId(addressId);
        AddressResponse response = addressService.update(user,request);

        return WebResponse.<AddressResponse>builder().data(response).build();
    }

    @GetMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<AddressResponse> get(User user,
                                            @PathVariable("contactId") String contactId,
                                            @PathVariable("addressId") String addressId)
    {
        AddressResponse response = addressService.get(user, contactId, addressId);
        return WebResponse.<AddressResponse>builder().data(response).build();
    }

    @DeleteMapping(
            path = "/api/contacts/{contactId}/addresses/{addressId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> delete(User user,
                                            @PathVariable("contactId") String contactId,
                                            @PathVariable("addressId") String addressId)
    {
        String response = addressService.delete(user, contactId, addressId);
        return WebResponse.<String>builder().data(response).build();
    }

    @GetMapping(
            path = "/api/contacts/{contactId}/addresses",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<List<AddressResponse>> lists(User user,
                                                    @PathVariable String contactId)
    {
        List<AddressResponse> responses = addressService.lists(user,contactId);
        return WebResponse.<List<AddressResponse>>builder().data(responses).build();
    }
}
