package maa.restful.resolver;

import jakarta.servlet.http.HttpServletRequest;
import maa.restful.entity.User;
import maa.restful.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

/*
* Fungsi ArgumentResolver adalah untuk memfilter setiap Params yang mempunyai Type Data sama pada @RestController
* setiap @RestController yang methodnya punya argument dengan Type Data class User maka akan langsung dimasukin kesini
* */

@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserRepository userRepository;

    // Ini tempat cocokin typeData yang diharapkan
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.equals(parameter.getParameterType()); // Cocokin apakah Params pada method Controller butuh type data class User
    }

    // Ini adalah logic apa yang ingin dilakukan, setiap Params dengan tipe data Class User
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest(); // Ini ngambil Request

        // Ambil token dari Header
        String token = servletRequest.getHeader("X-API-TOKEN");

        // Kalo tokennya kosong artinya blm login gk bisa akses API
        if (token == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized");

        // Cari user dengan token yang sama
        User user = userRepository.findFirstByToken(token).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized"));

        // Masa expired token tidak boleh telat
        if (user.getTokenExpiredAt() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Unauthorized");
        }

        // Kalo semuanya lolos maka kirim User saat ini sebagai Params untuk masing-masing Controller yang paramnya (User user)
        return user;
    }
}

// Setelah membuat ArgumentResolver kita harus mendaftarkannya di dalam WebConfiguration
// Bikin dulu WebConfigurationnya
