package com.ssafy.special.crew.domain;


import com.ssafy.special.member.domain.Member;
import lombok.*;

import javax.persistence.*;

@Data
@Entity(name = "crew_recommend_vote")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CrewRecommendVote {
    // 사용자 seq
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crew_recommend_vote_seq")
    private Long crewRecommendVoteSeq;

    // crew_seq
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="crew_recommend_food_seq")
    private CrewRecommendFood crewRecommendFood;
    // crew_seq
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_seq")
    private Member member;

    @Builder
    public CrewRecommendVote(Long crewRecommendVoteSeq, CrewRecommendFood crewRecommendFood, Member member) {
        this.crewRecommendVoteSeq = crewRecommendVoteSeq;
        this.crewRecommendFood = crewRecommendFood;
        this.member = member;
    }
}
