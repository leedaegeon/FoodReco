package com.ssafy.special.member.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
public class UserCodeDto {

    private String email;
    private String code;
}
