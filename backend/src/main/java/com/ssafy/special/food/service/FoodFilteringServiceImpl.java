package com.ssafy.special.food.service;

import com.ssafy.special.food.domain.FoodIngredient;
import com.ssafy.special.food.domain.Ingredient;
import com.ssafy.special.food.dto.response.RecommendFoodDto;
import com.ssafy.special.food.repository.FoodIngredientRepository;
import com.ssafy.special.member.domain.Member;
import com.ssafy.special.member.domain.MemberAllergy;
import com.ssafy.special.member.domain.MemberFoodPreference;
import com.ssafy.special.member.repository.MemberAllergyRepository;
import com.ssafy.special.member.repository.MemberFoodPreferenceRepository;
import com.ssafy.special.member.repository.MemberRecommendRepository;
import com.ssafy.special.member.service.FoodFilteringService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Setter
@Service
@RequiredArgsConstructor
@Transactional
public class FoodFilteringServiceImpl implements FoodFilteringService {
    private final MemberAllergyRepository memberAllergyRepository;
    private final FoodIngredientRepository foodIngredientRepository;
    private final MemberFoodPreferenceRepository memberFoodPreferenceRepository;
    private final MemberRecommendRepository memberRecommendRepository;


    @Override
    public List<RecommendFoodDto> filteringByAllergy(List<RecommendFoodDto> recommendFoodDtoList, Member member) {
        List<MemberAllergy> memberAllergyList = memberAllergyRepository.findMemberAllergiesByMember_MemberSeq(member.getMemberSeq());

        if (memberAllergyList.isEmpty()) {
            return recommendFoodDtoList;
        }
        Set<Long> allergyIngredients = memberAllergyList.stream()
                .map(MemberAllergy::getIngredient) // Ingredient 객체를 얻습니다.
                .map(Ingredient::getIngredientSeq) // Ingredient 객체에서 ingredientSeq를 얻습니다.
                .collect(Collectors.toSet());
        return recommendFoodDtoList.stream()
                .filter(recommendFood -> {
                    List<FoodIngredient> foodIngredientList = foodIngredientRepository.findFoodIngredientsByFood_FoodSeqAndIngredient_IsAllergy(recommendFood.getFoodSeq(), 1);

                    log.info("FoodIngredientList for foodSeq {}: {}", recommendFood.getFoodSeq(), foodIngredientList);

                    Set<Long> foodAllergyIngredients = foodIngredientList.stream()
                            .map(FoodIngredient::getIngredient)
                            .map(Ingredient::getIngredientSeq)
                            .collect(Collectors.toSet());

                    log.info("AllergyIngredients: {}", allergyIngredients);
                    log.info("FoodAllergyIngredients for foodSeq {}: {}", recommendFood.getFoodSeq(), foodAllergyIngredients);

                    boolean isDisjoint = Collections.disjoint(allergyIngredients, foodAllergyIngredients);
                    log.info("Is Disjoint for foodSeq {}: {}", recommendFood.getFoodSeq(), isDisjoint);

                    return isDisjoint;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendFoodDto> filteringByHate(List<RecommendFoodDto> recommendFoodDtoList, Member member) {
        List<MemberFoodPreference> memberFoodHateList = memberFoodPreferenceRepository.findMemberFoodPreferencesByMember_MemberSeqAndPreferenceType(member.getMemberSeq(), 1);
        if (!memberFoodHateList.isEmpty()) {
            return recommendFoodDtoList;
        }
        Set<Long> hatedFoodSeqs = memberFoodHateList.stream()
                .map(memberFoodPreference -> memberFoodPreference.getFood().getFoodSeq())
                .collect(Collectors.toSet());

        return recommendFoodDtoList.stream()
                .filter(recommendFoodDto -> !hatedFoodSeqs.contains(recommendFoodDto.getFoodSeq()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendFoodDto> filteringByRecently(List<RecommendFoodDto> recommendFoodDtoList, Member member) {
        List<Long> memberRecommendsWithinOneWeek = memberRecommendRepository.findMemberRecommendsWithinOneWeek(member.getMemberSeq(), LocalDateTime.now());

        if (memberRecommendsWithinOneWeek.isEmpty()) {
            return recommendFoodDtoList;
        }

        Set<Long> recentlyRecommendedFoodSeqs = new HashSet<>(memberRecommendsWithinOneWeek);

        return recommendFoodDtoList.stream()
                .filter(recommendFoodDto -> !recentlyRecommendedFoodSeqs.contains(recommendFoodDto.getFoodSeq()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendFoodDto> filteringByAllergy(List<MemberAllergy> memberAllergyList, List<RecommendFoodDto> recommendFoodDtoList) {
        if (memberAllergyList.isEmpty()) {
            return recommendFoodDtoList;
        }
        Set<Long> allergyIngredients = memberAllergyList.stream()
                .map(MemberAllergy::getIngredient) // Ingredient 객체를 얻습니다.
                .map(Ingredient::getIngredientSeq) // Ingredient 객체에서 ingredientSeq를 얻습니다.
                .collect(Collectors.toSet());
        return recommendFoodDtoList.stream()
                .filter(recommendFood -> {
                    List<FoodIngredient> foodIngredientList = foodIngredientRepository.findFoodIngredientsByFood_FoodSeqAndIngredient_IsAllergy(recommendFood.getFoodSeq(), 1);

                    log.info("FoodIngredientList for foodSeq {}: {}", recommendFood.getFoodSeq(), foodIngredientList);

                    Set<Long> foodAllergyIngredients = foodIngredientList.stream()
                            .map(FoodIngredient::getIngredient)
                            .map(Ingredient::getIngredientSeq)
                            .collect(Collectors.toSet());

                    log.info("AllergyIngredients: {}", allergyIngredients);
                    log.info("FoodAllergyIngredients for foodSeq {}: {}", recommendFood.getFoodSeq(), foodAllergyIngredients);

                    boolean isDisjoint = Collections.disjoint(allergyIngredients, foodAllergyIngredients);
                    log.info("Is Disjoint for foodSeq {}: {}", recommendFood.getFoodSeq(), isDisjoint);

                    return isDisjoint;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendFoodDto> filteringByHate(List<MemberFoodPreference> memberFoodHateList, List<RecommendFoodDto> recommendFoodDtoList) {
        if (!memberFoodHateList.isEmpty()) {
            return recommendFoodDtoList;
        }
        Set<Long> hatedFoodSeqs = memberFoodHateList.stream()
                .map(memberFoodPreference -> memberFoodPreference.getFood().getFoodSeq())
                .collect(Collectors.toSet());

        return recommendFoodDtoList.stream()
                .filter(recommendFoodDto -> !hatedFoodSeqs.contains(recommendFoodDto.getFoodSeq()))
                .collect(Collectors.toList());
    }
    @Override
    public List<RecommendFoodDto> filteringByRecently(List<Long> memberRecommendsWithinOneWeek, List<RecommendFoodDto> recommendFoodDtoList) {
        if (memberRecommendsWithinOneWeek.isEmpty()) {
            return recommendFoodDtoList;
        }

        Set<Long> recentlyRecommendedFoodSeqs = new HashSet<>(memberRecommendsWithinOneWeek);

        return recommendFoodDtoList.stream()
                .filter(recommendFoodDto -> !recentlyRecommendedFoodSeqs.contains(recommendFoodDto.getFoodSeq()))
                .collect(Collectors.toList());
    }
}
