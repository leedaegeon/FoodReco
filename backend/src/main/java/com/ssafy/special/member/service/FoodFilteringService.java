package com.ssafy.special.member.service;

import com.ssafy.special.food.dto.response.RecommendFoodDto;
import com.ssafy.special.member.domain.Member;
import com.ssafy.special.member.domain.MemberAllergy;
import com.ssafy.special.member.domain.MemberFoodPreference;

import java.util.List;

public interface FoodFilteringService {
    List<RecommendFoodDto> filteringByAllergy(List<RecommendFoodDto> recommendFoodDtoList, Member member);

    List<RecommendFoodDto> filteringByHate(List<RecommendFoodDto> recommendFoodDtoList, Member member);

    List<RecommendFoodDto> filteringByRecently(List<RecommendFoodDto> recommendFoodDtoList, Member member);

    List<RecommendFoodDto> filteringByAllergy(List<MemberAllergy> memberAllergyList, List<RecommendFoodDto> recommendFoodDtoList);

    List<RecommendFoodDto> filteringByHate(List<MemberFoodPreference> memberFoodHateList, List<RecommendFoodDto> recommendFoodDtoList);

    List<RecommendFoodDto> filteringByRecently(List<Long> memberRecommendsWithinOneWeek, List<RecommendFoodDto> recommendFoodDtoList);
}
