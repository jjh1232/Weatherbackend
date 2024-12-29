package com.example.firstproject.Handler;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;

public interface WeatherServiceHandler {
	
	public int getcount(String keyword);
	
	public List<String> weathersearch(String keyword,int page);
	
	public WeatherdataEntity getweatherdata(String reg1,String reg2,String reg3);

	public Page<WeatherregionEntity> getweatherregion(String keword,Pageable page);

}
