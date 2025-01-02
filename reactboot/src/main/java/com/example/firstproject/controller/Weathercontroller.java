package com.example.firstproject.controller;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.firstproject.Dto.Weather.AreaRequestDto;
import com.example.firstproject.Dto.Weather.WeatherDataDto;
import com.example.firstproject.Dto.Weather.frontweather;
import com.example.firstproject.Dto.Weather.userregionDto;
import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;
import com.example.firstproject.Service.WeatherService;
import com.example.firstproject.aop.Logoutano;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class Weathercontroller {

	@Autowired
	private WeatherService weatherservice;
	
	
	/*
	@PostMapping("/weatherresult")
	public List<AreaRequestDto> getareastep(AreaRequestDto areaRequestDto){
		AreaRequestDTO coordinate = this.weatherservice.getCoordinate(areaRequestDto.getAreacode());
        areaRequestDTO.setNx(coordinate.getNx());
        areaRequestDTO.setNy(coordinate.getNy());

        List<WeatherDTO> weatherList = this.weatherservice.getWeather(areaRequestDto);
        return weatherList;
		
	}
*/
	
	
	//지역정보 찾아서 리턴
	@GetMapping("/open/regionsearch")
	@Logoutano //aop제외위한커스텀어노
	public List<String> weathersearch(@RequestParam String keyword,@RequestParam(defaultValue="1") int page) {
		System.out.println("검색값" +keyword);
		String newkeyword=keyword.replace(" ", "");
		List<String> search=weatherservice.weathersearch(newkeyword,page);
		System.out.println("search:"+search);
		
		return search;
		
	}

	//레기온 데이터로 당일 날씨 가져오기 
	@GetMapping("/open/weatherdata")
	@Logoutano 
	public List<frontweather> weatherdata(
			@RequestParam(defaultValue = "서울특별시  종로구  청운효자동") String region,
			@RequestParam(defaultValue = "60") String gridx,
			@RequestParam(defaultValue = "127") String gridy
			) throws URISyntaxException, UnsupportedEncodingException {
		log.info(region.toString());
		System.out.println("날씨데이터시작");
		
		String reg1=region.split("  ")[0];
		log.info(reg1);
		String reg2=region.split("  ")[1];
		log.info(reg2);
		String reg3=region.split("  ")[2];
		log.info(reg3);
		
		
		System.out.println(reg1+"  "+reg2+"  "+reg3 );
		
		List<frontweather> info=weatherservice.getweatherdata(reg1,reg2,reg3,gridx,gridy);
		System.out.println("날씨api컨트롤러가보냄");
		return info;
	
	}
	//
	@GetMapping("/open/regioncount")
	@Logoutano 
	public int regioncount(@RequestParam String keyword) {
		System.out.println(keyword);
		String newkeyword=keyword.replace(" ", "");
		int count=weatherservice.regionget(newkeyword);
		System.out.println(count);
		return count;
	}

	//지역검색 페이지데이터 리턴 주소찾기
	@GetMapping("/open/pageregion")
	@Logoutano 
	public Page<userregionDto> allget(@RequestParam String keyword,@RequestParam(defaultValue="1") int page){
		String newkeyword=keyword.replace(" ", "");//띄어쓰기없애기!
		System.out.println(newkeyword);
		Page<userregionDto> data=weatherservice.getpageweather(newkeyword, page);
		System.out.println("가공:"+data);
		return data;
	}
}
	/*
	 @PostMapping(value = "/board/weatherStep.do")
	    @ResponseBody
	    public List<AreaRequestDto> getAreaStep(@RequestParam Map<String, String> params)
	    {
	        return this.weatherservice.getArea(params);
	    }
}*/
