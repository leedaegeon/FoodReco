package com.ssafy.special.crew.service;

import com.ssafy.special.crew.domain.Crew;
import com.ssafy.special.crew.domain.CrewMember;
import com.ssafy.special.crew.domain.CrewRecommend;
import com.ssafy.special.crew.domain.CrewRecommendFood;
import com.ssafy.special.food.domain.Food;
import com.ssafy.special.member.domain.MemberAllergy;
import com.ssafy.special.member.domain.MemberFoodPreference;
import com.ssafy.special.crew.dto.response.CrewRecommendHistoryByFoodDto;
import com.ssafy.special.food.dto.response.RecommendFoodDto;
import com.ssafy.special.crew.dto.response.VoteRecommendDto;
import com.ssafy.special.crew.repository.CrewMemberRepository;
import com.ssafy.special.crew.repository.CrewRecommendFoodRepository;
import com.ssafy.special.crew.repository.CrewRecommendRepository;
import com.ssafy.special.crew.repository.CrewRepository;
import com.ssafy.special.food.repository.FoodRepository;
import com.ssafy.special.member.repository.MemberAllergyRepository;
import com.ssafy.special.member.repository.MemberFoodPreferenceRepository;
import com.ssafy.special.member.repository.MemberRecommendRepository;
import com.ssafy.special.member.repository.MemberRepository;
import com.ssafy.special.etc.service.SseService;
import com.ssafy.special.member.service.FoodFilteringService;
import com.ssafy.special.member.service.FoodRecommendService;
import com.ssafy.special.member.service.MemberFeedbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrewRecommendService {
    private final MemberRepository memberRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final CrewRecommendRepository crewRecommendRepository;
    private final MemberFeedbackService memberFeedbackService;
    private final MemberRecommendRepository memberRecommendRepository;
    private final MemberAllergyRepository memberAllergyRepository;
    private final MemberFoodPreferenceRepository memberFoodPreferenceRepository;
    private final CrewRecommendFoodRepository crewRecommendFoodRepository;
    private final FoodRepository foodRepository;
    private final TaskScheduler taskScheduler;
    private final SseService sseService;

    private final FoodFilteringService foodFilteringService;
    private final FoodRecommendService foodRecommendService;
//    @org.springframework.beans.factory.annotation.Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//    @Value("${cloud.aws.region.static}")
//    private String region;

    public void recommendFood(Long crewSeq) throws EntityNotFoundException, Exception {
        Crew crew = crewRepository.findByCrewSeq(crewSeq)
                .orElseThrow(() -> new EntityNotFoundException("해당 그룹을 찾을 수 없습니다."));

        // 분석 시작
        crew.setStatus("분석중");
        crewRepository.save(crew);

        /*
         * 여기에서 멤버 별 추천 리스트를 가져옴
         * RecommendFoodDto.builder().member(Member.class).recommendFoods(List<Food.class>).build
         * DB에 저장
         */
        CrewRecommend crewRecommend = CrewRecommend.builder()
                .crew(crew)
                .recommendAt(LocalDateTime.now())
                .build();
        crewRecommend = crewRecommendRepository.save(crewRecommend);

        List<RecommendFoodDto> crewRecommendFoodList = new ArrayList<>();
        List<MemberAllergy> crewAllergyList = new ArrayList<>();
        List<MemberFoodPreference> crewMemberPreferenceList = new ArrayList<>();
        List<Long> crewMemberRecommendFoodWithinOneWeekList = new ArrayList<>();
        for(CrewMember c : crew.getCrewMembers()){
            crewRecommendFoodList.addAll(foodRecommendService.getRecommendList(c.getMember().getMemberSeq(),LocalDateTime.now(),c.getMember().getEmail(), c.getMember().getActivity(), "맑음"));
            crewAllergyList.addAll(memberAllergyRepository.findMemberAllergiesByMember_MemberSeq(c.getMember().getMemberSeq()));
            crewMemberPreferenceList.addAll(memberFoodPreferenceRepository
                    .findMemberFoodPreferencesByMember_MemberSeqAndPreferenceType(c.getMember().getMemberSeq(), 1));
            crewMemberRecommendFoodWithinOneWeekList.addAll(memberRecommendRepository.findMemberRecommendsWithinOneWeek(c.getMember().getMemberSeq(), LocalDateTime.now()));
        }
//        2. 알러지 필터링
        crewRecommendFoodList = foodFilteringService.filteringByAllergy(crewAllergyList, crewRecommendFoodList);

//        3. 차단 필터링
        crewRecommendFoodList = foodFilteringService.filteringByHate(crewMemberPreferenceList, crewRecommendFoodList);
//        4. 최근에 먹음 처리
        crewRecommendFoodList = foodFilteringService.filteringByRecently(crewMemberRecommendFoodWithinOneWeekList, crewRecommendFoodList);
        Map<Long, Integer> cnt = new HashMap<>();
        for(RecommendFoodDto food : crewRecommendFoodList){
            cnt.put(food.getFoodSeq(),cnt.getOrDefault(food.getFoodSeq(),0) + 1);
        }

        // Map을 List로 변환
        List<Map.Entry<Long, Integer>> list = new ArrayList<>(cnt.entrySet());

        // List를 값으로 정렬 (내림차순)
        Collections.sort(list, new Comparator<Map.Entry<Long, Integer>>() {
            @Override
            public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue()); // 내림차순
            }
        });
        int foodCnt = 0;
        List<CrewRecommendHistoryByFoodDto> recommendList = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : list) {
            if(foodCnt > 9) break;
            Food food = foodRepository.findFoodByFoodSeq(entry.getKey());
            crewRecommendFoodRepository.save(CrewRecommendFood.builder()
                            .food(food)
                            .crewRecommend(crewRecommend)
                    .build());
            foodCnt++;

            CrewRecommendHistoryByFoodDto crewRecommendHistoryByFoodDto = CrewRecommendHistoryByFoodDto.builder()
                    .foodSeq(food.getFoodSeq())
//                    .foodImg("https://" + bucket + ".s3." + region + ".amazonaws.com/" + food.getImg())
                    .foodImg(null)
                    .foodName(food.getName())
                    .foodVoteCount(0)
                    .isVote(false)
                    .build();
            recommendList.add(crewRecommendHistoryByFoodDto);
        }
        Collections.sort(recommendList, Comparator.comparingLong(CrewRecommendHistoryByFoodDto::getFoodSeq));
        VoteRecommendDto voteRecommendDto =VoteRecommendDto.builder()
                .crewRecommendSeq(crewRecommend.getCrewRecommendSeq())
                .foodList(recommendList)
                .crewRecommendTime(LocalDateTime.now())
                .build();


        // sse로 투표 시작이라는 알림 전송 + FCM 으로 백그라운드의 그룹원들에게 제공
        sseService.chageVote(crewSeq,"start",voteRecommendDto);
        crew.setStatus("투표중");
        crewRepository.save(crew);

        // 5분뒤 종료하는 스케줄러 실행
        taskScheduler.schedule(() -> endVote(crew), Instant.now().plusSeconds(60));
    }

    public void endVote(Crew crew) {
        log.info("투표 종료");
        crew.setStatus("투표전");
        crewRepository.save(crew);
        for(CrewMember c : crew.getCrewMembers()){
            c.setCheckVote(0);
            crewMemberRepository.save(c);
        }
        // 그룹원들에게 종료되었다는 sse 알림 + 백그라운 그룹원들에게 FCM 알림

        sseService.chageVote(crew.getCrewSeq(),"end",null);

    }




}
