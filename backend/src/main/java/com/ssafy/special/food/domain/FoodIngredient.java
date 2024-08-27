package com.ssafy.special.food.domain;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Getter
@Entity(name = "food_ingredient")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@IdClass(FoodId.class)
public class FoodIngredient {

    // food_seq, PK값
    @Id
    @ManyToOne
    @JoinColumn(name= "food_seq")
    @JsonBackReference
    private Food food;

    // 그룹 seq
    @Id
    @ManyToOne
    @JoinColumn(name= "ingredient_seq")
    @JsonBackReference
    private Ingredient ingredient;


    @Builder
    public FoodIngredient(Food food, Ingredient ingredient) {
        this.food = food;
        this.ingredient = ingredient;
    }
}
