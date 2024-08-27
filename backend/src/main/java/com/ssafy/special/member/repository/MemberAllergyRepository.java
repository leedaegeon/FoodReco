package com.ssafy.special.member.repository;

import com.ssafy.special.member.domain.MemberAllergy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface MemberAllergyRepository extends JpaRepository<MemberAllergy, Long> {
    List<MemberAllergy> findMemberAllergiesByMember_MemberSeq(Long memberSeq);

}
