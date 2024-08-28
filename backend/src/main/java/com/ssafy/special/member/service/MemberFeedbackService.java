package com.ssafy.special.member.service;

import com.ssafy.special.food.domain.Food;
import com.ssafy.special.member.domain.Member;
import com.ssafy.special.member.domain.MemberFoodPreference;
import com.ssafy.special.member.domain.MemberRecommend;
import com.ssafy.special.food.dto.request.FeedbackDto;
import com.ssafy.special.food.repository.FoodRepository;
import com.ssafy.special.member.repository.MemberFoodPreferenceRepository;
import com.ssafy.special.member.repository.MemberRecommendRepository;
import com.ssafy.special.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Setter
@Service
@RequiredArgsConstructor
@Transactional
public class MemberFeedbackService {

    private final MemberRecommendRepository memberRecommendRepository;
    private final MemberRepository memberRepository;
    private final WebClient webClient;
    private final MemberService memberService;
    private final FoodRepository foodRepository;
    private final MemberFoodPreferenceRepository memberFoodPreferenceRepository;
    private final MemberGoogleAuthService memberGoogleAuthService;
    private final FoodRecommendService foodRecommendService;
    //    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    @Value("${cloud.aws.region.static}")
//    private String region;



    /* 묵시적 피드백 반영 */
    /*
    차단:0,
	패스:2,
	상세보기:3,
	좋아요:4
//    */
//    피드백시 다음 음식이 어떤건지 프론트에서 알려줌
//    그 음식을 추천받은 DB 추가하기 -> 푸드 레이팅에 1로
//    만약 다음 음식이 없는 경우 아무 처리x
    
    public void implicitFeedback(String memberEmail, FeedbackDto feedbackDto, Long nextFoodSeq, int googleSteps, String weather) throws Exception {


        Member member = memberService.getMember(memberEmail);

        Food nowFood = foodRepository.findFoodByFoodSeq(feedbackDto.getFoodSeq());


        List<MemberRecommend> memberRecommend = getMemberRecommends(member.getMemberSeq(), nowFood.getFoodSeq());
        int lastFoodRating = getLastFoodRating(member, member.getMemberSeq(), nowFood, feedbackDto.getFeedback(), memberRecommend);
        if(feedbackDto.getFeedback() == 4) addNowFoodToFavorite(member, member.getMemberSeq(), nowFood);


        int totalSteps = member.getActivity() + googleSteps;
        if(weather == null) weather = "맑음";
//        피드백 반영
//        패스할때 이전에 상세보기였는지, 좋아요였는지,
//        언제 추천받았고 패스를 한건지 시간을 고려해서 패스 반영
        addFeedback(weather, member, nowFood, feedbackDto.getFeedback(), lastFoodRating, totalSteps, memberRecommend);


//        다음에 볼 음식을 추천받은 리스트에 추가하기
//        상세보기, 좋아요가 아니면서 마지막 추천 음식이 아닌 경우
        addNotBadFoodToRecommend(nextFoodSeq, weather, nextFoodSeq.equals(0L) ? true: false, member, feedbackDto.getFeedback(), totalSteps);

    }

    public int getLastFoodRating(Member member, Long memberSeq, Food nowFood, int feedback, List<MemberRecommend> memberRecommend) {
        int lastFoodRating = -1;
        if (!memberRecommend.isEmpty()) lastFoodRating = memberRecommend.get(0).getFoodRating();

        if (feedback == 0) {
            addNowFoodToHate(member, memberSeq, nowFood);
            lastFoodRating = 0;
        } else if (feedback == 2) {
            lastFoodRating = 2;
            log.info("패스");
        } else if (feedback == 3) {
            lastFoodRating = 3;
            log.info("상세보기");
        } else if (feedback == 4) {
            lastFoodRating = 4;
            log.info("좋아요");
//            현재 음식을 좋아하는 리스트에 추가하기
        }
        return lastFoodRating;
    }

    public void addFeedback(String weather, Member member, Food nowFood, int feedback, int lastFoodRating, int totalSteps, List<MemberRecommend> memberRecommend) {
        if (memberRecommend.isEmpty()) {
            log.info("현재 보고있는 처음 본 음식 피드백 추가");
            MemberRecommend memberRecommend2 = MemberRecommend.builder()
                    .member(member)
                    .food(nowFood)
                    .weather(weather)
                    .activityCalorie(totalSteps)
                    .foodRating(lastFoodRating)
                    .recommendAt(LocalDateTime.now())
                    .build();
            memberRecommendRepository.save(memberRecommend2);
            return;
        }

        int lastStatus = memberRecommend.get(0).getFoodRating();
        if(feedback == 2 && lastStatus == 3){
//                  지난번에 상세를 봤지만 이번에는 패스이므로 pass로 업데이트 -> 일주일을 기준으로 데이터 수정 또는 데이터 추가
            Duration duration = Duration.between(memberRecommend.get(0).getRecommendAt(), LocalDateTime.now());
            long differenceInDays = duration.toDays();
            if (differenceInDays < 8)
                memberRecommendRepository.updateFoodRating(2, memberRecommend.get(0).getMemberRecommendSeq());
            else {
                MemberRecommend memberRecommend2 = MemberRecommend.builder()
                        .member(member)
                        .food(nowFood)
                        .weather(weather)
                        .activityCalorie(totalSteps)
                        .foodRating(2)
                        .recommendAt(LocalDateTime.now())
                        .build();
                memberRecommendRepository.save(memberRecommend2);
            }
        }else if(feedback == 2 && lastStatus == 4){
//                  좋아하는 음식이지만 현재는 패스 -> 좋아한지 일주일 넘었으면 새로운 데이터 추가
            Duration duration = Duration.between(memberRecommend.get(0).getRecommendAt(), LocalDateTime.now());
            long differenceInDays = duration.toDays();

            if (differenceInDays >= 8) {
                MemberRecommend memberRecommend2 = MemberRecommend.builder()
                        .member(member)
                        .food(nowFood)
                        .weather(weather)
                        .activityCalorie(totalSteps)
                        .foodRating(4)
                        .recommendAt(LocalDateTime.now())
                        .build();
                memberRecommendRepository.save(memberRecommend2);
            }
        }

        if(feedback != 2){
            Duration duration = Duration.between(memberRecommend.get(0).getRecommendAt(), LocalDateTime.now());
            long differenceInDays = duration.toDays();
            if (differenceInDays < 8)
                memberRecommendRepository.updateFoodRating(lastFoodRating, memberRecommend.get(0).getMemberRecommendSeq());
            else {
                MemberRecommend memberRecommend2 = MemberRecommend.builder()
                        .member(member)
                        .food(nowFood)
                        .weather(weather)
                        .activityCalorie(totalSteps)
                        .foodRating(lastFoodRating)
                        .recommendAt(LocalDateTime.now())
                        .build();
                memberRecommendRepository.save(memberRecommend2);
            }
        }




    }

    public void addNotBadFoodToRecommend(Long nextFoodSeq, String weather, boolean isLast, Member member, int feedback, int totalSteps) {
        if ((feedback != 3 || feedback != 4) && !isLast) {
            addRecommendHistory(nextFoodSeq, member, weather, totalSteps);
            log.info("다음 음식 추천 히스토리 추가 완료");
        }
    }

    public void addNowFoodToFavorite(Member member, Long memberSeq, Food nowFood) {
        Optional<MemberFoodPreference> memberFoodPreferenceList =
                memberFoodPreferenceRepository.findMemberFoodPreferenceByMember_MemberSeqAndFood_FoodSeq(memberSeq, nowFood.getFoodSeq());
        if (memberFoodPreferenceList.isEmpty()) {
            MemberFoodPreference memberFoodPreference = MemberFoodPreference.builder()
                    .member(member)
                    .food(nowFood)
                    .preferenceType(0)
                    .build();
            memberFoodPreferenceRepository.save(memberFoodPreference);
        } else {
            memberFoodPreferenceRepository.updateMemberFoodPreference(0, memberSeq, nowFood.getFoodSeq());
        }
    }

    public void addNowFoodToHate(Member member, Long memberSeq, Food nowFood) {

        log.info("차단");
//           현재 음식을 차단 리스트에 추가
        Optional<MemberFoodPreference> memberFoodPreferenceList = memberFoodPreferenceRepository.findMemberFoodPreferenceByMember_MemberSeqAndFood_FoodSeq(memberSeq, nowFood.getFoodSeq());
        if (memberFoodPreferenceList.isEmpty()) {
            MemberFoodPreference memberFoodPreference = MemberFoodPreference.builder()
                    .member(member)
                    .food(nowFood)
                    .preferenceType(1)
                    .build();
            memberFoodPreferenceRepository.save(memberFoodPreference);
        } else {
            memberFoodPreferenceRepository.updateMemberFoodPreference(1, memberSeq, nowFood.getFoodSeq());
        }
    }

    public List<MemberRecommend> getMemberRecommends(Long memberSeq, Long foodSeq) {
        return memberRecommendRepository.findLatestMemberRecommend(memberSeq, foodSeq, PageRequest.of(0,1));
    }

    public void addRecommendHistory(Long recommendFoodDtoList, Member member, String weather, int totalSteps) {
        Food firstFood = foodRepository.findFoodByFoodSeq(recommendFoodDtoList);

        MemberRecommend memberRecommend = MemberRecommend.builder()
                .member(member)
                .food(firstFood)
                .recommendAt(LocalDateTime.now())
                .weather(weather)
                .activityCalorie(totalSteps)
                .foodRating(1)
                .build();
        memberRecommendRepository.save(memberRecommend);
    }


}
