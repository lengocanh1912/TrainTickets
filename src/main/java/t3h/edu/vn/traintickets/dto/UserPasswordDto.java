package t3h.edu.vn.traintickets.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordDto implements Serializable {
    private String newPassword;
    private String rePassword;

}
