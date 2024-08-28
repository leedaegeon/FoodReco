package com.ssafy.special.member.service;

import com.ssafy.special.food.dto.response.RecentRecommendFoodDto;
import com.ssafy.special.food.dto.response.RecentRecommendFoodResult;
import com.ssafy.special.food.dto.response.RecommendFoodDto;
import com.ssafy.special.food.dto.response.RecommendFoodResultDto;
import com.ssafy.special.member.domain.Member;
import com.ssafy.special.member.dto.request.UserTasteDto;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public interface FoodRecommendService {
    List<RecommendFoodDto> getRecommendList(Long memberSeq, LocalDateTime now, String memberEmail, int googleSteps, String nowWeather);

    Mono<List<RecommendFoodDto>> sendPostRequestAndReceiveRecommendFoodList(List<RecentRecommendFoodDto> recentlyRecommendedFood);

    List<RecommendFoodResultDto> recommendFood(String memberEmail, int googleSteps, String weather);

    }
