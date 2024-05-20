package maa.restful.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchContactRequest {

    private String name;

    private String phone;

    @Email
    private String email;

    @NotNull
    private Integer page;

    @NotNull
    private Integer size;
}
