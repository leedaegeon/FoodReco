package com.ssafy.special.crew.repository;

import com.ssafy.special.crew.domain.Crew;
import com.ssafy.special.crew.domain.CrewRecommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewRecommendRepository extends JpaRepository<CrewRecommend, Long> {
    List<CrewRecommend> findAllByCrewOrderByRecommendAtDesc(Crew crew);
    CrewRecommend findByCrewRecommendSeq(Long crewRecommendSeq);
    CrewRecommend findFirstByCrewOrderByRecommendAtDesc(Crew crew);
}
