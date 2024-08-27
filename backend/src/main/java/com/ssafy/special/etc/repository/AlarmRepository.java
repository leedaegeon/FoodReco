package com.ssafy.special.etc.repository;

import com.ssafy.special.etc.domain.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

}
