package com.kpjunaid.dto;

import com.kpjunaid.annotation.ValidEmail;
import com.kpjunaid.annotation.ValidPassword;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmailDto {
    @ValidEmail
    private String email;

    @ValidPassword
    private String password;
}
