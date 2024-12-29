package com.example.firstproject.Dto;

import java.time.LocalDateTime;

import com.example.firstproject.Entity.LoginHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistoryDto {

	private Long id;
	private String userid;
	private LocalDateTime logindt;
	private String clientip;
	private String userdata;
	
	private boolean islogin;
	
	public static LoginHistoryDto fromEntity(LoginHistory loginhistory) {
		return LoginHistoryDto.builder()
				.id(loginhistory.getId())
				.userid(loginhistory.getUserid())
				.logindt(loginhistory.getLogindt())
				.clientip(loginhistory.getClientip())
				.userdata(loginhistory.getUserdata())
				.islogin(loginhistory.isIslogin())
				.build();
	}
}
