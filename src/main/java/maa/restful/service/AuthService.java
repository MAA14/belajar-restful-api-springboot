package maa.restful.service;

import jakarta.transaction.Transactional;
import maa.restful.entity.User;
import maa.restful.model.LoginUserRequest;
import maa.restful.model.TokenResponse;
import maa.restful.repository.UserRepository;
import maa.restful.security.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public TokenResponse login(LoginUserRequest request) throws Exception {
        // Validation
        validationService.validate(request);

        // Cek user ada atau tidak
        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password is Wrong"));

        // Cek password benar atau tidak
        if (!BCrypt.checkpw(request.getPassword(),user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or Password is Wrong");
        }

        // Simpan data
        user.setToken(UUID.randomUUID().toString());
        user.setTokenExpiredAt(next7Days());
        userRepository.save(user);

        return TokenResponse.builder()
                .token(user.getToken())
                .tokenExpiredAt(user.getTokenExpiredAt())
                .build();
    }

    @Transactional
    public void logout(User user) {
        // Tokennya dihapus biar gk bisa akses API
        user.setToken(null);
        user.setTokenExpiredAt(0L);
        userRepository.save(user);
    }

    private Long next7Days() {
        // Ambil Hari saat user login
        Date today = new Date();

        // Ubah Hari jadi milisecond supaya bisa dilakukan operasi MTK
        Long todayInMili = today.getTime(); // ini hasilnya Long atau Milisecond
        // Output dari today.getTime() adalah milisecond yang dimulai semenjak 1 Januari 2024
        // Note : Tahunnya mengikuti tahun setiap tahun

        // 7 Hari dalam miliseconds || Hari * Jam * Menit * Detik * Miliseconds
        Long sevenDaysInMili = (long) (7 * 24 * 60 * 60 * 1000);
        return todayInMili + sevenDaysInMili;
    }

    // Ini contoh code untuk menentukan waktu expired dari PZN
    private Long next30Days() {
        return System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
    }
}
