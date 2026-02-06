package t3h.edu.vn.traintickets.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDTO {

    private Long userId;
    private String userName;
    private String email;
    private String phone;
    private Boolean online;

}
