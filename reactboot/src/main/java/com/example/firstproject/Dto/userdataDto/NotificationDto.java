package com.example.firstproject.Dto.userdataDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationDto {

	private Long id;
	private String message;
	private String red;
	private Long noticeid;
	private boolean isread;
}
