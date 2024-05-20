package maa.restful.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateContactRequest {

    @JsonIgnore // Pake ini biar kalo ada yang ngirim Id lewat Request Body gk bakal dibaca
    @NotBlank
    @Size(max = 100)
    private String id;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    @Email
    private String email;

    @Size(max = 100)
    private String phone;

}
