package com.ssafy.special.crew.dto.response;

import com.ssafy.special.crew.dto.response.CrewRecommendHistoryByFoodDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VoteRecommendDto {
    private Long crewRecommendSeq;
    private List<CrewRecommendHistoryByFoodDto> foodList;
    private LocalDateTime crewRecommendTime;
    @Builder
    public VoteRecommendDto(Long crewRecommendSeq, List<CrewRecommendHistoryByFoodDto> foodList, LocalDateTime crewRecommendTime) {
        this.crewRecommendSeq = crewRecommendSeq;
        this.foodList = foodList;
        this.crewRecommendTime = crewRecommendTime;
    }
}
