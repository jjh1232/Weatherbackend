package com.example.firstproject.Handler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;
import com.example.firstproject.Repository.WeatherReposiotry;

@Service
public class WeatherServiceHandlerImpl implements WeatherServiceHandler{

	@Autowired
	WeatherReposiotry weatherrepository;
	
	@Override
	public List<String> weathersearch(String keyword,int page) {
		// TODO Auto-generated method stub
		List<String> search=weatherrepository.weathersearch(keyword,page);
		
		System.out.println("핸들러 서치"+search.toString());
		
		return search;
	}

	@Override
	public WeatherdataEntity getweatherdata(String reg1, String reg2, String reg3) {
		// TODO Auto-generated method stub
		WeatherdataEntity data=weatherrepository.getweatherdata(reg1, reg2, reg3);
		System.out.println(data.toString());
		return data;
	}

	@Override
	public int getcount(String keyword) {
		// TODO Auto-generated method stub
		int count = weatherrepository.getcount(keyword);
		return count;
	}

	@Override
	public Page<WeatherregionEntity> getweatherregion(String keword,Pageable pageable) {
		// TODO Auto-generated method stub
		System.out.println("핸들러");
		
		Page<WeatherregionEntity> data=weatherrepository.pageweather(keword,pageable);
		return data;
	}

}
