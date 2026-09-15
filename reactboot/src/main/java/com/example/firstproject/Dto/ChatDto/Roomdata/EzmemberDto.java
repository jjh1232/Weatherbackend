package com.example.firstproject.Dto.ChatDto.Roomdata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EzmemberDto {

	private Long userid;
	private String email;
	private String nickname;
	private String profileurl;

	/** 채팅 발신자로 쓸 값. 입장·퇴장 같은 시스템 메시지는 보낸 회원이 없다(member_id NULL). */
	public static final String SYSTEM = "System";

	/**
	 * 메시지의 member 로 발신자를 만든다. null 이면 시스템 발신자.
	 * 예전엔 c.getMember().getId() 를 바로 불러서, 시스템 메시지가 섞인 방은 조회가 터지거나
	 * (조인으로 걸러) 입장 메시지가 방 안에서 사라졌다.
	 */
	public static EzmemberDto of(com.example.firstproject.Entity.MemberEntity m) {
		if (m == null) {
			return EzmemberDto.builder().email(SYSTEM).nickname(SYSTEM).build();
		}
		return EzmemberDto.builder()
				.userid(m.getId())
				.email(m.getUsername())
				.nickname(m.getNickname())
				.profileurl(m.getProfileimg())
				.build();
	}
}
