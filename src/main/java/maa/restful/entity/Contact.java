package maa.restful.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    private String id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    private String phone;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username")
    private User user;
    // name = nama column yang dijadiin Foreign Key
    // User user; User adalah nama Table yang ingin dihubungkan, sedangkan user adalah nama hubungannya (nanti dimapped make nama ini)
    // dan referencedColumnName = nama column yang ada ditable User (users)

    @OneToMany(mappedBy = "contact")
    private List<Address> addresses;
}