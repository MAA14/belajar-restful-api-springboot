package maa.restful.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import maa.restful.entity.Address;
import maa.restful.entity.Contact;
import maa.restful.entity.User;
import maa.restful.model.AddressResponse;
import maa.restful.model.CreateAddressRequest;
import maa.restful.model.UpdateAddressRequest;
import maa.restful.repository.AddressRepository;
import maa.restful.repository.ContactRepository;
import maa.restful.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AddressService {

    @Autowired
    ValidationService validationService;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    ContactRepository contactRepository;

    @Autowired
    UserRepository userRepository;

    private Address getAddressByUserAndContactIdAndAddressId(User user, String contactId, String addressId) {
        // Check apakah contactnya ada?
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact is not found"));

        // Check apakah addressnya ada?
        return addressRepository.findFirstByContactAndId(contact, addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Address is not found"));
    }

    private AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .province(address.getProvince())
                .city(address.getCity())
                .street(address.getStreet())
                .id(address.getId())
                .build();
    }

    @Transactional
    public AddressResponse create(User user, CreateAddressRequest request) throws Exception{
        validationService.validate(request);

        // Check apakah contactnya ada?
        Contact contact = contactRepository.findFirstByUserAndId(user, request.getContactId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact is not found"));

        // Bikin address
        Address address = new Address();
        address.setId(UUID.randomUUID().toString());
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setContact(contact); // Jangan lupa contactnya harus sesuai

        // Save ke database
        addressRepository.save(address);

        return toAddressResponse(address);
    }

    @Transactional
    public AddressResponse update(User user, UpdateAddressRequest request) {
        validationService.validate(request);

        Address address = getAddressByUserAndContactIdAndAddressId(user, request.getContactId(), request.getAddressId());

        /**
         * -- Kenapa gk make if else buat bikin Optional Update?
         * Karena kita make method PUT pada @RestController, yang best practicenya untuk mengubah seluruh data
         *
         * -- Trus kalo update sebagian data aja make apa methodnya?
         * Kita bisa make method PATCH kalo mau update dengan bentuk Optional Update
         */
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setProvince(request.getProvince());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());

        // Save update
        addressRepository.save(address);

        return toAddressResponse(address);
    }

    public AddressResponse get(User user, String contactId, String addressId) {
        Address address = getAddressByUserAndContactIdAndAddressId(user, contactId, addressId);

        return toAddressResponse(address);
    }

    @Transactional
    public String delete(User user, String contactId, String addressId) {
        Address address = getAddressByUserAndContactIdAndAddressId(user, contactId, addressId);

        addressRepository.delete(address);
        return "OK";
    }

    public List<AddressResponse> lists(User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user,contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Contact is not found"));

        List<Address> addresses = addressRepository.findAllByContact(contact);

        // Convert List<Address> menjadi List<AddressResponse>
        return addresses.stream()
                .map(this::toAddressResponse)
                .toList();
    }
}
