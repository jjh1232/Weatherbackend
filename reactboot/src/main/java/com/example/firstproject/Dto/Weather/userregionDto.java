package com.example.firstproject.Dto.Weather;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
public class userregionDto {

	
	String region;
	
	String gridx;
	
	String gridy;
	
	
}
