package com.ssafy.special.crew.repository;

import com.ssafy.special.crew.domain.Crew;
import com.ssafy.special.crew.domain.CrewId;
import com.ssafy.special.crew.domain.CrewMember;
import com.ssafy.special.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrewMemberRepository extends JpaRepository<CrewMember, CrewId> {
    @Override
    List<CrewMember> findAllById(Iterable<CrewId> crewIds);

    Optional<CrewMember> findByCrew(Crew crew);
    Optional<CrewMember> findByMemberAndCrew(Member member,Crew crew);
}
