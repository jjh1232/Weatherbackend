package com.example.firstproject.Dto.Weather;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;




@NoArgsConstructor
@Getter
@Setter
public class frontweather {

	private String PTY;
	
	private String RN1;
	
	private String SKY;
	
	private String T1H;
	
	private String REH;
	
	private String WSD;
	
	private String date;
	private String time;
	
	
}
