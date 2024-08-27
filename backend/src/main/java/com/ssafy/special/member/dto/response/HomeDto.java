package com.ssafy.special.member.dto.response;

import com.ssafy.special.food.dto.response.RecentFoodDto;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HomeDto {
    private List<TypeRateDto> typeRates;
    private List<RecentFoodDto> recentFoods;
    @Builder
    public HomeDto(List<TypeRateDto> typeRates, List<RecentFoodDto> recentFoods) {
        this.typeRates = typeRates;
        this.recentFoods = recentFoods;
    }
}
