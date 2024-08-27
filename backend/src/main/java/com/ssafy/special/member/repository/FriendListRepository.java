package com.ssafy.special.member.repository;

import com.ssafy.special.crew.domain.FriendList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendListRepository extends JpaRepository<FriendList, Long>{
    List<FriendList> findByOneMemberSeqOrOtherMemberSeq(Long oneMemberSeq, Long otherMemberSeq);
}
