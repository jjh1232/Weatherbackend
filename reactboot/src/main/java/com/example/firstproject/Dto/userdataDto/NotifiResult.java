package com.example.firstproject.Dto.userdataDto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotifiResult<T> {

	private List<T> content; //알림페이지데이터
	private int currentpage;
	private int totalpage;
	private long totalElements;
}
