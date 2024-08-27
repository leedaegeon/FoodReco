package com.ssafy.special.crew.repository;

import com.ssafy.special.crew.domain.Crew;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrewRepository extends JpaRepository<Crew, Long> {

    Optional<Crew> findByCrewSeq(Long crewSeq);
}
