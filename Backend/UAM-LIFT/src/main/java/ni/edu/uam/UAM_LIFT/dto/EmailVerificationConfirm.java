package ni.edu.uam.UAM_LIFT.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationConfirm {
    @NotBlank
    private String correo;

    @NotBlank
    private String code;
}

