package maa.restful.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class UpdateAddressRequest {

    @JsonIgnore
    @NotBlank
    @Size(max = 100)
    private String contactId;

    @JsonIgnore
    @NotBlank
    @Size(max = 100)
    private String addressId;

    @Size(max = 200)
    private String street;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    @NotBlank
    private String country;

    @Size(max = 100)
    private String postalCode;
}
