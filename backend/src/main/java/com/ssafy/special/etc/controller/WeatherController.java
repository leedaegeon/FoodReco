package com.ssafy.special.etc.controller;



import com.ssafy.special.etc.dto.request.WeatherRequestDto;
import com.ssafy.special.etc.dto.response.WeatherStatus;
import com.ssafy.special.etc.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WeatherController {


    private final WeatherStatus weatherStatus;
    private final WeatherService weatherService;

    @Value("${weather.service-key}")
    private String serviceKey;

    @GetMapping("/weather")
    public void getWeather(@RequestBody WeatherRequestDto weatherRequestDto) throws Exception {

        weatherService.getWeather(weatherRequestDto.getLon(),weatherRequestDto.getLet());



    }


}

