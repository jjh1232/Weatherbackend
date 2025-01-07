package com.example.firstproject.Repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.Weather.AreaRequestDto;
import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;
@Repository

public interface WeatherReposiotry extends JpaRepository<WeatherdataEntity, String>{

	@Query(nativeQuery = true,value = "Select count(*) from weatherregion where concat(step1,step2,step3) like %:keyword%")
	public int getcount(@Param("keyword") String keyword);
	
	@Query(nativeQuery = true,value = "Select concat(step1,\"  \",step2,\"  \",step3,\"  \") from  weatherregion where concat(step1,step2,step3) like %:keyword% limit :start,10")
	public List<String> weathersearch(@Param("keyword") String keyword,@Param("start")int page);
	
	@Query(nativeQuery=true ,value="select * from weatherregion where step1 like %:reg1% and step2 like %:reg2% and step3 like %:reg3%")
	public WeatherdataEntity getweatherdata(String reg1,String reg2,String reg3);
	
	@Query(nativeQuery=true,value= "Select * from  weatherregion where concat(step1,step2,step3) like %:keyword%")
	public Page<WeatherregionEntity> pageweather(@Param("keyword") String keyword,Pageable page);
			
}
