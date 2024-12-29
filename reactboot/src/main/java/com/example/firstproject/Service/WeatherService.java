package com.example.firstproject.Service;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.firstproject.Dto.Weather.AreaRequestDto;
import com.example.firstproject.Dto.Weather.WeatherDataDto;
import com.example.firstproject.Dto.Weather.frontweather;
import com.example.firstproject.Dto.Weather.userregionDto;
import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;

public interface WeatherService {

	
	public int regionget(String keyword);
	
	List<String> weathersearch(String keyword,int page);
	
	List<frontweather> getweatherdata(String reg1,String reg2,String reg3) throws URISyntaxException, UnsupportedEncodingException;
	
	//List<WeatherDto> getWeather(AreaRequestDto areaRequestDto);
	
	AreaRequestDto getCoordinate(String areacode);
	
	Page<userregionDto> getpageweather(String keyword,int page);
	//ResponseEntity<WeatherApiResponseDto> requestWeatherApi(AreaRequestDto arearequestDto);
}
