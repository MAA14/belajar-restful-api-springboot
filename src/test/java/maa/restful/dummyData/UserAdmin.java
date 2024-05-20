package maa.restful.dummyData;

import maa.restful.entity.User;
import maa.restful.model.LoginUserRequest;
import maa.restful.repository.UserRepository;
import maa.restful.security.BCrypt;
import maa.restful.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserAdmin extends User {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    private User admin;

    // Dummy User untuk Login Testing
    private void build() throws Exception {
        // Kalo udah pernah dibikin, gk usah bikin lagi
        if (this.admin == null) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(BCrypt.hashpw("admin",BCrypt.gensalt())); // Kayak gini biar ke Hashing
            user.setName("admin");
            // user.setPassword("admin"); // Gk bisa mentah gini nanti Error karena kita validasi make BCrypt

            userRepository.save(user);
            this.admin = userRepository.findById("admin").orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or Password is Wrong"));
        }
    }

    public User get() throws Exception {
        if (this.admin == null) {
            this.build();
        }

        return this.admin;
    }

    public void login() throws Exception {
        // Kalo blm di Build maka build dulu
        if (this.admin == null) {
            this.build();
        }

        // Baru lakukan login
        LoginUserRequest loginUserRequest = LoginUserRequest.builder().username("admin").password("admin").build();
        authService.login(loginUserRequest);

        // Update adminnya karena tokennya ke update pas login
        this.admin = userRepository.findById("admin").orElse(null);
    }

    // Ambil dari database secara langsung, biar datanya up to date
    public User getFresh() {
        this.admin = userRepository.findById("admin").orElse(null);
        return this.admin;
    }

    public void reset() {
        this.admin = null;
    }
}
