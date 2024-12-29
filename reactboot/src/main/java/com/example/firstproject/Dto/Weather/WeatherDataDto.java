package com.example.firstproject.Dto.Weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherDataDto {

	
	private String category;
	private String value;
	private String date;
	private String time;
	
	
	
	
	

}
