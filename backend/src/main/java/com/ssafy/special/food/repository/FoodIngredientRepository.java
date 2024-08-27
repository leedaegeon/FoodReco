package com.ssafy.special.food.repository;

import com.ssafy.special.food.domain.FoodId;
import com.ssafy.special.food.domain.FoodIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodIngredientRepository extends JpaRepository<FoodIngredient, FoodId> {
    List<FoodIngredient> findFoodIngredientsByFood_FoodSeq(Long foodSeq);

    List<FoodIngredient> findFoodIngredientsByFood_FoodSeqAndIngredient_IsAllergy(Long foodSeq, int isAllergy);

}
