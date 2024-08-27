package com.ssafy.special.crew.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VoteDto {
    private Long crewSeq;
    private Long crewRecommendSeq;
    private Long foodSeq;

   @Builder
    public VoteDto(Long crewSeq, Long crewRecommendSeq, Long foodSeq) {
        this.crewSeq = crewSeq;
        this.crewRecommendSeq = crewRecommendSeq;
        this.foodSeq = foodSeq;
    }
}
