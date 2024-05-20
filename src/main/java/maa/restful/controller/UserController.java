package maa.restful.controller;

import maa.restful.entity.User;
import maa.restful.model.RegisterUserRequest;
import maa.restful.model.UpdateUserRequest;
import maa.restful.model.UserResponse;
import maa.restful.model.WebResponse;
import maa.restful.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    // Inget WebResponse<T> itu bikinan kita sendiri yang isinya data + errors
    @PostMapping(
            path = "/api/users", // Setting Path
            consumes = MediaType.APPLICATION_JSON_VALUE, // Type Data Requestnya apa
            produces = MediaType.APPLICATION_JSON_VALUE // Type Data Responsenya apa
    )
    public WebResponse<String> register(@RequestBody RegisterUserRequest request) throws Exception{
        userService.register(request);
        return WebResponse.<String>builder().data("Ok").build();
    }

    @GetMapping(
            path = "/api/users/current",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    // Params user akan diisi otomatis oleh UserArgumentResolver
    public WebResponse<UserResponse> get(User user) {
        // user pada UserService berasal dari Controller, dan params user pada Controller dari UserArgumentResolver
        UserResponse currentUser = userService.get(user);
        return WebResponse.<UserResponse>builder()
                .data(currentUser)
                .build();
    }

    @PatchMapping(
            path = "/api/users/current",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    // Header X-API-Token dihandle oleh UserArgumentResolver
    public WebResponse<UserResponse> update(User user, @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.update(user,request);
        return WebResponse.<UserResponse>builder()
                .data(response)
                .build();
    }
}
