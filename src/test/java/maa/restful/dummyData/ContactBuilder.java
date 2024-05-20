package maa.restful.dummyData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import maa.restful.entity.Contact;
import maa.restful.entity.User;
import maa.restful.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactBuilder
{
    @Autowired
    private ContactRepository contactRepository;

    public Contact build(User user,String firstName, String lastName) {
        Contact contact = new Contact();
        contact.setId(UUID.randomUUID().toString());
        contact.setFirstName(firstName);
        contact.setLastName(lastName);
        contact.setEmail(firstName + "@example.com");
        contact.setPhone("123123123");
        contact.setUser(user);

        contactRepository.save(contact);
        return contact;
    }
}
