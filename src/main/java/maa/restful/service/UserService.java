package maa.restful.service;

import jakarta.transaction.Transactional;
import maa.restful.entity.User;
import maa.restful.model.RegisterUserRequest;
import maa.restful.model.UpdateUserRequest;
import maa.restful.model.UserResponse;
import maa.restful.repository.UserRepository;
import maa.restful.security.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidationService validationService;

    // Transactional artinya kalo gagal datanya gk disimpen ke database dan harus isi/kirim ulang supaya bisa kesimpan
    @Transactional
    public void register(RegisterUserRequest request) throws Exception {
        // Validation
        validationService.validate(request);

        // Setup Exception kalo datanya double (udah ada username yang sama)
        if (userRepository.existsById(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Username is already registered");
        }

        // Kalo gk ada error
        User user = new User();
        user.setName(request.getName());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt())); // Hashing Password
        user.setUsername(request.getUsername());

        userRepository.save(user); // Save to database
    }

    // Params user akan diisi otomatis oleh Controller pada Endpoint "/api/users/current"
    public UserResponse get(User user){
        return UserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    @Transactional
    public UserResponse update(User user, UpdateUserRequest request) {
        if (Objects.nonNull(request.getName())) {
            user.setName(request.getName());
        }

        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(BCrypt.hashpw(request.getPassword(),BCrypt.gensalt()));
        }

        userRepository.save(user);
        return UserResponse.builder()
                .username(user.getUsername())
                .name(request.getName())
                .build();
    }
}
