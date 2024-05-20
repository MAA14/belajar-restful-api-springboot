package maa.restful.repository;

import maa.restful.entity.Contact;
import maa.restful.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository  extends JpaRepository<Contact, String>, JpaSpecificationExecutor<Contact> { // JpaSpecification buat bikin Query yang lebih advance

    // Bikin Query untuk mencari contact berdasarkan user saat ini
    Optional<Contact> findFirstByUserAndId(User user, String id);
}
