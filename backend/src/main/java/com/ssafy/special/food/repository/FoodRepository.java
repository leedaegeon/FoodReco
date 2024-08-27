
package com.ssafy.special.food.repository;

import com.ssafy.special.food.domain.Food;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Food findByName(String name);

    Food findFoodByFoodSeq(Long foodSeq);
}
