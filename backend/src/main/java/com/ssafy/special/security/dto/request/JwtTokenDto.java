package com.ssafy.special.security.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtTokenDto {
    private String email;
    private String refreshToken;
}
