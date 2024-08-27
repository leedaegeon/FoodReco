
package com.ssafy.special.security.dto.request;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@Data
public class UserSignUpDto {
    private String email;
    private String password;
    private String nickname;
    private int age;
    private String sex;
    private int height;
    private int weight;
    private int activity;
    private List<String> favoriteList;
    private List<String> hateList;
    private List<String> allergyList;
    public UserSignUpDto(String email, String password, String nickname, int age, String sex, int height, int weight, int activity, List<String> favoriteList, List<String> hateList, List<String> allergyList) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.age = age;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.activity = activity;
        this.favoriteList = favoriteList;
        this.hateList = hateList;
        this.allergyList = allergyList;
    }
}