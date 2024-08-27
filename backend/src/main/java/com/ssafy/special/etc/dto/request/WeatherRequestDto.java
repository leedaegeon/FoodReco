package com.ssafy.special.etc.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Getter
public class WeatherRequestDto {
    private Double lon;
    private Double let;

}
