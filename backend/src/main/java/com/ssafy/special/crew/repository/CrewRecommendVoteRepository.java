package com.ssafy.special.crew.repository;

import com.ssafy.special.crew.domain.CrewRecommendFood;
import com.ssafy.special.crew.domain.CrewRecommendVote;
import com.ssafy.special.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrewRecommendVoteRepository extends JpaRepository<CrewRecommendVote, Long> {

    List<CrewRecommendVote> findAllByCrewRecommendFood(CrewRecommendFood crewRecommendFood);
    Optional<CrewRecommendVote> findByCrewRecommendFoodAndMember(CrewRecommendFood crewRecommendFood, Member member);
}
