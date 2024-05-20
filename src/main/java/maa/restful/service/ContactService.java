package maa.restful.service;

import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import maa.restful.entity.Address;
import maa.restful.entity.Contact;
import maa.restful.entity.User;
import maa.restful.model.ContactResponse;
import maa.restful.model.CreateContactRequest;
import maa.restful.model.SearchContactRequest;
import maa.restful.model.UpdateContactRequest;
import maa.restful.repository.AddressRepository;
import maa.restful.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private AddressRepository addressRepository;

    private ContactResponse toContactResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .build();
    }

    @Transactional
    public ContactResponse create(User user, CreateContactRequest request) {
        // Lakukan Validasi
        validationService.validate(request);

        // Buat contact baru
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());

        // Simpan sebagai contact untuk user saat ini
        contact.setUser(user);

        // Simpen ke database
        contactRepository.save(contact);
        return toContactResponse(contact);
    }

    @Transactional
    public ContactResponse update(User user, UpdateContactRequest request) throws Exception{
        // Lakukan validasi
        validationService.validate(request);

        // Cari kontaknya ada gk, dan apakah kontak tersebut adalah milik User saat ini
        Contact contact = contactRepository.findFirstByUserAndId(user,request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found"));

        // Kalo kontaknya ada maka lakukan update
        if (Objects.nonNull(request.getFirstName())) {
            contact.setFirstName(request.getFirstName());
        }

        if (Objects.nonNull(request.getLastName())) {
            contact.setLastName(request.getLastName());
        }

        if (Objects.nonNull(request.getEmail())) {
            contact.setEmail(request.getEmail());
        }

        if (Objects.nonNull(request.getPhone())) {
            contact.setPhone(request.getPhone());
        }

        // Simpan dan Return
        contactRepository.save(contact);
        return toContactResponse(contact);
    }

    @Transactional
    public String delete(User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user,contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Kita harus hapus dulu semua addressnya (Address terhubung ke Contact berdasarkan ID dengan konsep Many (Address) to One (Contact))

        /** @IMPORTANT
         ** Kalo konsepnya One to Many gk perlu dihapus dulu data yang terhubung
         ** Kalo konsepnya Many to Many harus dihapus dulu data yang terhubung
         **/

        addressRepository.deleteAllByContact(contact); // Hapus semua address yang terhubung
        contactRepository.delete(contact); // baru hapus contactnya
        return "OK";
    }

    public ContactResponse get(User user, String contactId) {
        Contact contact = contactRepository.findFirstByUserAndId(user, contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact is not found"));

        return toContactResponse(contact);
    }

    @Transactional
    public Page<ContactResponse> search(User user, SearchContactRequest request) {
            // Specification adalah method untuk membuat advance query, terutama menggunakan WHERE
            Specification<Contact> specification = ((root, query, builder) -> {
                List<Predicate> predicates = new ArrayList<>(); // jakarta.persistence.criteria.Predicate;

                if (Objects.nonNull(request.getName())) {
                    predicates.add(builder.or( // Optional Query
                            builder.like(root.get("firstName"), "%"+ request.getName() +"%"),
                            builder.like(root.get("lastName"), "%" + request.getName() + "%")
                    ));
                }

                predicates.add(builder.equal(root.get("user"), user)); // Mandatory query

                if (Objects.nonNull(request.getEmail())) {
                    predicates.add(builder.like(root.get("email"), "%"+ request.getEmail() + "%")); // Optional
                }

                if (Objects.nonNull(request.getPhone())) {
                    predicates.add(builder.like(root.get("phone"), "%" + request.getPhone() +"%")); // Optional
                }

                return query.where(predicates.toArray(new Predicate[]{})).getRestriction(); // getRestriction() method supaya JPA bisa menexecute querynya
                // Ini return dari fungsi Specification

                /**
                 * SELECT * FROM Contact
                 * WHERE user = ?  -- ini Mandatory makanya dihubungkan dengan logic AND
                 * AND (
                 *     (firstName LIKE '%name%' OR lastName LIKE '%name%')  -- Name search (if name provided)
                 *   OR email LIKE '%email%'  -- Email search (if email provided) -- kenapa column email sama phone make OR bukan AND?
                 *   OR phone LIKE '%phone%'  -- Phone search (if phone provided) -- karena didalam if condition yang artinya Optional
                 * )
                 */

                /**
                 * Mandatory query = builder.{logic} yang pasti selalu ada di dalam query, dihubungkan dengan AND oleh predicate.add()
                 * Optional query = builder.{logic} yang bisa tidak ada di dalam query, dihubungkan dengan OR oleh predicate.add()
                 */

                /**
                 * jadi Mandatory atau Optional tidak ditentukan oleh urutan builder.{logic}
                 * tapi ditentukan oleh logic code misalnya if condition
                 */
            });

        // Setting pagging
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize()); // (page saat ini, maks data per-page)
        Page<Contact> contacts = contactRepository.findAll(specification, pageable); // (Specification, Pageable)

        // Convert Contact to ContactResponse
        List<ContactResponse> contactResponses = contacts.getContent().stream()
                .map(this::toContactResponse) // sama aja kayak map(contact -> toContactResponse(contact))
                .toList(); // Convert jadiin List

        // PageImpl<>( contentNyaApa, PageAble, totalElement)
        return new PageImpl<>(contactResponses, pageable, contacts.getTotalElements());
    }
}

/**
 * SELECT *
 * FROM contact
 * WHERE user_id = {user_id}
 * AND email = {email} AND phone = {phone} AND id <= 200
 * LIMIT {page_size} OFFSET {page_number * page_size};
 */

/**
 * public static Specification<Contact> findByUserAndEmailAndPhone(Long userId, String email, String phone) {
 *         return (root, query, builder) -> {
 *             return builder.and(
 *                 builder.equal(root.get("user").get("id"), userId),
 *                 builder.equal(root.get("email"), email),
 *                 builder.equal(root.get("phone"), phone),
 *                 builder.lessThanOrEqualTo(root.get("id"), 200)
 *             );
 *         };
 *     }
 */