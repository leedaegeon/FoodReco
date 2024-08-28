package com.ssafy.special.food.service;

import com.ssafy.special.food.domain.Food;
import com.ssafy.special.food.dto.response.RecentRecommendFoodDto;
import com.ssafy.special.food.dto.response.RecentRecommendFoodResult;
import com.ssafy.special.food.dto.response.RecommendFoodDto;
import com.ssafy.special.food.dto.response.RecommendFoodResultDto;
import com.ssafy.special.food.repository.FoodRepository;
import com.ssafy.special.member.domain.Member;
import com.ssafy.special.member.dto.request.UserTasteDto;
import com.ssafy.special.member.repository.MemberRecommendRepository;
import com.ssafy.special.member.repository.MemberRepository;
import com.ssafy.special.member.service.FoodFilteringService;
import com.ssafy.special.member.service.FoodRecommendService;
import com.ssafy.special.member.service.MemberFeedbackService;
import com.ssafy.special.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodRecommendServiceImpl implements FoodRecommendService {
    private final MemberRecommendRepository memberRecommendRepository;
    private final MemberService memberService;
    private final WebClient webClient;
    private final FoodFilteringService foodFilteringService;
    private final FoodRepository foodRepository;
    private final MemberFeedbackService memberFeedbackService;
    public List<RecommendFoodDto> getRecommendList(Long memberSeq, LocalDateTime now, String memberEmail, int googleSteps, String nowWeather) {

//        지난 1~2주 사이에 추천 받은 음식을 기반으로 추천
        List<RecentRecommendFoodResult> recentlyRecommendedFood = memberRecommendRepository.findRecentlyRecommendedFood(memberSeq, now);

        if (nowWeather == null) {
            nowWeather = "맑음";
        }

        Member member = memberService.getMember(memberEmail);
        int totalSteps = member.getActivity() + googleSteps;

        Set<RecentRecommendFoodDto> recommendedFoods = new HashSet<>();
        recommendedFoods = getSetRecentlyRecommendedFood(recommendedFoods, recentlyRecommendedFood);

//          좋아하는 음식 리스트
        List<UserTasteDto> userPreferenceFoods = memberService.getUserPreference(memberEmail, 0);
        recommendedFoods = unionRecentAndPreference(recommendedFoods, userPreferenceFoods);

        List<RecentRecommendFoodDto> distinctedRecommendedFoods = recommendedFoods.stream().distinct().collect(Collectors.toList());

//        현재상황과 유사한 활동량과 날씨를 추출
        List<RecentRecommendFoodResult> simmilarFoods = new ArrayList<>();
        simmilarFoods = getSimmilarFoods(memberSeq, nowWeather, totalSteps, simmilarFoods);


        Set<RecentRecommendFoodDto> similarTotal = new HashSet<>();
        similarTotal = makeSimilarTotal(distinctedRecommendedFoods, simmilarFoods, similarTotal);

        return sendPostRequestAndReceiveRecommendFoodList(Arrays.asList((RecentRecommendFoodDto) similarTotal)).block();
    }

    private Set<RecentRecommendFoodDto> makeSimilarTotal(List<RecentRecommendFoodDto> distinctedRecommendedFoods, List<RecentRecommendFoodResult> simmilarFoods, Set<RecentRecommendFoodDto> similarAdded) {
        similarAdded.addAll(distinctedRecommendedFoods);
        List<RecentRecommendFoodDto> similarFoodList = simmilarFoods.stream()
                .map(result -> new RecentRecommendFoodDto(result.getFoodSeq(), result.getName()))
                .collect(Collectors.toList());

        similarAdded.addAll(similarFoodList);
        return similarAdded;
    }

    private List<RecentRecommendFoodResult> getSimmilarFoods(Long memberSeq, String nowWeather, int totalSteps, List<RecentRecommendFoodResult> simmilarFoods) {
        if (totalSteps >= 1000)
            simmilarFoods = memberRecommendRepository.findSimilarRecommendedFood(memberSeq, nowWeather, totalSteps - 1000, totalSteps + 1000);
        else if (simmilarFoods.size() == 0 && totalSteps >= 2000) {
            simmilarFoods = memberRecommendRepository.findSimilarRecommendedFood(memberSeq, nowWeather, totalSteps - 2000, totalSteps + 2000);
        } else if (simmilarFoods.size() == 0 && totalSteps >= 3000) {
            simmilarFoods = memberRecommendRepository.findSimilarRecommendedFood(memberSeq, nowWeather, totalSteps - 3000, totalSteps + 3000);
        }
        return simmilarFoods;
    }

    private Set<RecentRecommendFoodDto> unionRecentAndPreference(Set<RecentRecommendFoodDto> recommendedSet, List<UserTasteDto> userFavoriteList) {
        if (userFavoriteList.size() > 0) {
            List<RecentRecommendFoodDto> favoriteList = userFavoriteList.stream()
                    .map(RecentRecommendFoodDto::new)
                    .collect(Collectors.toList());
            recommendedSet.addAll(favoriteList);
        }
        return recommendedSet;
    }

    private Set<RecentRecommendFoodDto> getSetRecentlyRecommendedFood(Set<RecentRecommendFoodDto> recommendedSet, List<RecentRecommendFoodResult> recentlyRecommendedFood) {
        if (recentlyRecommendedFood.size() > 0) {
            List<RecentRecommendFoodDto> recentlyRecommendedFoodList = recentlyRecommendedFood.stream()
                    .map(result -> new RecentRecommendFoodDto(result.getFoodSeq(), result.getName()))
                    .collect(Collectors.toList());
            recommendedSet.addAll(recentlyRecommendedFoodList);
        }
        return recommendedSet;
    }
    public Mono<List<RecommendFoodDto>> sendPostRequestAndReceiveRecommendFoodList(List<RecentRecommendFoodDto> recentlyRecommendedFood) {

        return webClient.post()
                .uri("/fastapi/recommend")
                .bodyValue(recentlyRecommendedFood)
                .retrieve()
                .bodyToFlux(RecommendFoodDto.class)
                .collectList();
    }
    //    컨텐츠 기반 필터링
    public List<RecommendFoodResultDto> recommendFood(String memberEmail, int googleSteps, String weather) throws EntityNotFoundException {

        Member member = memberService.getMember(memberEmail);

        LocalDateTime now = LocalDateTime.now();
        int totalSteps = member.getActivity() + googleSteps;
        if(weather == null) {
            weather = "맑음";
        }
//
//
//        // 추천 음식 가져오기
        List<RecommendFoodDto> recommendFoodDtoList = getRecommendList(member.getMemberSeq(), now, memberEmail, googleSteps, weather);
//        log.info("추천 음식: " + recommendFoodDtoList.get(0).getName());

        if(recommendFoodDtoList.size()==0){
            log.info("추천 받은 음식이 없습니다.");
            return null;
        }

//        1. 첫 번째 음식은 추천받은 히스토리에 추가하기
        memberFeedbackService.addRecommendHistory(recommendFoodDtoList.get(0).getFoodSeq(), member, weather, totalSteps);

//        2. 알러지 필터링
        recommendFoodDtoList = foodFilteringService.filteringByAllergy(recommendFoodDtoList, member);

//        3. 차단 필터링

        recommendFoodDtoList = foodFilteringService.filteringByHate(recommendFoodDtoList, member);

//        4. 최근에 먹음 처리
        recommendFoodDtoList = foodFilteringService.filteringByRecently(recommendFoodDtoList, member);

//        5. 추천 리스트가 없는 경우 일단 보류
//            추천된 음식에 해당하는 음식 상세정보 합쳐서 프론트로 넘겨주기
        List<RecommendFoodResultDto> RecommendFoodResultList = getRecommendFoodResultList(recommendFoodDtoList);

        int endIndex = Math.min(RecommendFoodResultList.size(), 50);
        return RecommendFoodResultList.subList(0, endIndex);

    }

    private List<RecommendFoodResultDto> getRecommendFoodResultList(List<RecommendFoodDto> recommendFoodDtoList) {
        List<RecommendFoodResultDto> RecommendFoodResultList = new ArrayList<>();

        for (RecommendFoodDto recommendFoodDto : recommendFoodDtoList) {
            Food food = foodRepository.findFoodByFoodSeq(recommendFoodDto.getFoodSeq());

            int ingredientSim = (int) (recommendFoodDto.getSimilarity() * 100);
//                int nameSim = (int) (recommendFoodDto.getNameSimilarity() * 100);
            RecommendFoodResultDto recommendFoodResultDto = RecommendFoodResultDto.builder()
                    .recommendedFoodSeq(recommendFoodDto.getFoodSeq())
                    .ingredientSimilarity(recommendFoodDto.getSimilarity())
                    .recommendedFoodName(recommendFoodDto.getName())
                    .foodNameSimilarity(recommendFoodDto.getNameSimilarity())
                    .originalFoodName(recommendFoodDto.getOriginName())
                    .type(food.getType())
                    .category(food.getCategory())
                    .cookingMethod(food.getCookingMethod())
//                        .img("https://" + bucket + ".s3." + region + ".amazonaws.com/" + food.getImg())
                    .img(null)
                    .reason(recommendFoodDto.getName()
                            + "은(는) 지난번에 드신 "
                            + recommendFoodDto.getOriginName()
                            + "와(과) 재료 및 조리방식이 "
                            + ingredientSim + "% 유사하여 추천합니다.")
                    .build();
            RecommendFoodResultList.add(recommendFoodResultDto);
        }
        return RecommendFoodResultList;
    }


}
