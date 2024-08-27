package com.ssafy.special.crew.repository;

import com.ssafy.special.crew.domain.CrewRecommend;
import com.ssafy.special.crew.domain.CrewRecommendFood;
import com.ssafy.special.food.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrewRecommendFoodRepository extends JpaRepository<CrewRecommendFood, Long> {

    List<CrewRecommendFood> findAllByCrewRecommend(CrewRecommend crewRecommend);

    CrewRecommendFood findByCrewRecommendAndFood(CrewRecommend crewRecommend, Food food);
}
