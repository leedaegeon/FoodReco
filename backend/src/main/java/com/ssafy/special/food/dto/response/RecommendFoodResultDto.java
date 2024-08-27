package com.ssafy.special.food.dto.response;

import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class RecommendFoodResultDto {
    Long recommendedFoodSeq;
    String recommendedFoodName;
    String cookingMethod;
    String type;
    String category;
    String img;
    Double ingredientSimilarity;
    Double foodNameSimilarity;
    String originalFoodName;
    String reason;

}
