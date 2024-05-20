package maa.restful.repository;

import maa.restful.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
// JpaRepository<Class Table, Tipe data Primary Key column table>
public interface UserRepository extends JpaRepository<User, String> {

    // Bikin query sendiri
    Optional<User> findFirstByToken(String token);
}
