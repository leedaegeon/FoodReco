package com.ssafy.special.crew.dto.response;

import com.ssafy.special.member.dto.response.MemberInfoDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class FriendListDto {
    private String memberEmail;
    private List<MemberInfoDto> friendList;

    @Builder
    public FriendListDto(String memberEmail, List<MemberInfoDto> friendList) {
        this.memberEmail = memberEmail;
        this.friendList = friendList;
    }
}
