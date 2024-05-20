package maa.restful.model;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {
    // Update itu Optional jadi gk perlu @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 100) // Size kalo kosong tetep lolos
    private String password;
}
