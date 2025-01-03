package com.example.firstproject.Service;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Cache;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.firstproject.Dto.Weather.AreaRequestDto;
import com.example.firstproject.Dto.Weather.WeatherDataDto;
import com.example.firstproject.Dto.Weather.Weatherapiresponse;
import com.example.firstproject.Dto.Weather.frontweather;
import com.example.firstproject.Dto.Weather.userregionDto;
import com.example.firstproject.Entity.Weather.WeatherdataEntity;
import com.example.firstproject.Entity.Weather.WeatherregionEntity;
import com.example.firstproject.Handler.WeatherServiceHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
//@CacheConfig(cacheNames="weather")//클래스내부에서 code라는값으로 공통적으로사용
//근데 메소드에 밸류 따로주면 그걸로 인식하는듯?
public class WeatherServiceimpl implements WeatherService{

	@Autowired
	private WeatherServiceHandler weatherhandler;
	
	
	@Autowired
	private CacheManager cachemanager;
	

	


	@Override//캐시에담아봄
	
	public List<String> weathersearch(String keyword,int page) {
		// TODO Auto-generated method stub
		
		List<String> search=weatherhandler.weathersearch(keyword,page);
		System.out.println("리스트길이:"+search.size());
		return  search;
	}
	
	



	@Override   //콘피그에서만든캐쉬매니저네임
	@Cacheable(value = "getweather",key="#reg1+#reg2+#reg3",unless = "#result==null")
	public List<frontweather> getweatherdata(String reg1, String reg2, String reg3,String gridx,String gridy) throws URISyntaxException, UnsupportedEncodingException {
		// TODO Auto-generated method stub
		String servicekey ="1UxOsFtGRc1qt%2FBSr5YDb%2B%2BBfx9rWkUUCg9Pbt8%2BbpYlHmJLRPr4aiWZINe4hGjWTia37Y5QAVtOO9D%2B6HyRFA%3D%3D";
		String url="http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst";
		System.out.println("날씨api 겟데이터 서비스");
		
		Calendar cal1=Calendar.getInstance();
		cal1.add(Calendar.HOUR, -3);//-2시간 더하기 
		//음 데이터에서 시간언제기준인지 봐야알듯 나중에보자
		Date currentdate=new Date(cal1.getTimeInMillis());
		
		System.out.println(currentdate.toString());
		SimpleDateFormat simpleformatdate=new SimpleDateFormat("yyyyMMdd");//원하는데이터포맷지정가능한클래스
		SimpleDateFormat simpleformattime=new SimpleDateFormat("HH");//대문자아니면 0~12만사용함;
		String strnowdate=simpleformatdate.format(currentdate);
		String strnowtime=simpleformattime.format(currentdate)+"00";
		System.out.println("포맷후"+strnowdate);
		System.out.println("시간"+strnowtime);
		//그냥파람으로받기
		//WeatherdataEntity userdata=weatherhandler.getweatherdata(reg1, reg2, reg3);
		
		RestTemplate weather=new RestTemplate();
		HttpHeaders headers=new HttpHeaders();
		//headers.setContentType(new MediaType("application", "JSON", Charset.forName("UTF-8")));딱히명시없어서혹시햇는데없어도댐
		StringBuilder builder = new StringBuilder(url);
		builder.append("?"+"ServiceKey="+servicekey);
		builder.append("&"+"pageNo="+"1");
		builder.append("&"+"numOfRows="+"1000");
		builder.append("&"+"dataType="+"JSON");
		builder.append("&"+"base_date="+strnowdate);
		builder.append("&"+"base_time="+strnowtime);
		//builder.append("&"+"base_time="+strnowtime);
		builder.append("&"+"nx="+gridx);
		builder.append("&"+"ny="+gridy);
		System.out.println("url값"+builder);
		URI uri=new URI(builder.toString());
		
		HttpEntity<MultiValueMap<String,Object>> asd=new HttpEntity<>(headers);
		
		ResponseEntity response=weather.exchange(uri,
				HttpMethod.GET,
				asd,
				String.class
				);
		System.out.println("날씨api보냄");
		System.out.println(response.toString());
		
		Weatherapiresponse weatherresponse=null;//내부클래스를 static 으로 해줘야하는데 설명좀봐야할듯
		ObjectMapper obmap=new ObjectMapper();
		try {
		 weatherresponse=obmap.readValue(response.getBody().toString(), Weatherapiresponse.class);
		
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(weatherresponse.toString());
		//Map<String,Object> info=new HashMap<>();
		List<WeatherDataDto> item=new ArrayList();
		//String strnow=simpleformattime.format(currentdate);
		for(int i=0;i<weatherresponse.getResponse().getBody().getItems().getItem().size();i++) {
			if(weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("SKY")||weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("PTY")
					||weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("RN1")||weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("T1H")
					||weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("REH")||weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory().equals("WSD")
					) {
				
				// 강수형태(PTY) 코드 : (초단기) 없음(0), 비(1), 비/눈(2), 눈(3), 빗방울(5), 빗방울눈날림(6), 눈날림(7) 
				//RN! 1시간강수량
				//- 하늘상태(SKY) 코드 : 맑음(1), 구름많음(3), 흐림(4)
				//T1H 온도
				//REH 습도
				//WSD 풍속 
				String category=weatherresponse.getResponse().getBody().getItems().getItem().get(i).getCategory();
				String value=weatherresponse.getResponse().getBody().getItems().getItem().get(i).getFcstValue();
				String date=weatherresponse.getResponse().getBody().getItems().getItem().get(i).getFcstDate();
				String time=weatherresponse.getResponse().getBody().getItems().getItem().get(i).getFcstTime();
				
				WeatherDataDto data=WeatherDataDto.builder()
						.category(category)
						.value(value)
						.date(date)
						.time(time)
						.build();
				item.add(data);	
			}
			
		}
			//for문끝 여기서가공하는게나을듯 맵으로하는게나을듯?
			List<frontweather> time=new ArrayList<>();
			frontweather data1=new frontweather();
			frontweather data2=new frontweather();
			frontweather data3=new frontweather();
			frontweather data4=new frontweather();
			frontweather data5=new frontweather();
			frontweather data6=new frontweather();
			
			for(WeatherDataDto wea:item) { //쩔수없이 노가다..
				
				if(wea.getTime().equals(item.get(0).getTime())){
					
					if(wea.getCategory().toString().equals("SKY")){
						data1.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data1.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data1.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data1.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data1.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data1.setT1H(wea.getValue());
						data1.setDate(wea.getDate());
						data1.setTime(wea.getTime());
					}
					
					
									
					
				}else if(wea.getTime().equals(item.get(1).getTime())){
					if(wea.getCategory().toString().equals("SKY")){
						data2.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data2.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data2.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data2.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data2.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data2.setT1H(wea.getValue());
						data2.setDate(wea.getDate());
						data2.setTime(wea.getTime());
					}
					
					
				}else if(wea.getTime().equals(item.get(2).getTime())){
					
					if(wea.getCategory().toString().equals("SKY")){
						data3.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data3.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data3.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data3.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data3.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data3.setT1H(wea.getValue());
						data3.setDate(wea.getDate());
						data3.setTime(wea.getTime());
					}
					
				}else if(wea.getTime().equals(item.get(3).getTime())){
					
				
					if(wea.getCategory().toString().equals("SKY")){
						data4.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data4.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data4.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data4.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data4.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data4.setT1H(wea.getValue());
						data4.setDate(wea.getDate());
						data4.setTime(wea.getTime());
					}
					
				}else if(wea.getTime().equals(item.get(4).getTime())){
					
					if(wea.getCategory().toString().equals("SKY")){
						data5.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data5.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data5.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data5.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data5.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data5.setT1H(wea.getValue());
						data5.setDate(wea.getDate());
						data5.setTime(wea.getTime());
					}
					
				}else if(wea.getTime().equals(item.get(5).getTime())){
					
					if(wea.getCategory().toString().equals("SKY")){
						data6.setSKY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("PTY")){
						data6.setPTY(wea.getValue());
					}
					else if(wea.getCategory().toString().equals("RN1")){
						data6.setRN1(wea.getValue());
					}else if(wea.getCategory().toString().equals("REH")){
						data6.setREH(wea.getValue());
					}else if(wea.getCategory().toString().equals("WSD")){
						data6.setWSD(wea.getValue());
					}else if(wea.getCategory().toString().equals("T1H")){
						data6.setT1H(wea.getValue());
						data6.setDate(wea.getDate());
						data6.setTime(wea.getTime());
					}
					
					
				}
			
				
				
				
			}
			
		
			time.add(data1);
			time.add(data2);
			time.add(data3);
			time.add(data4);
			time.add(data5);
			time.add(data6);
		System.out.println("날씨데이터정리끝"+time);
		return time;
	}



	@Override
	public int regionget(String keyword) {
		// TODO Auto-generated method stub
		int count =weatherhandler.getcount(keyword);
		return count;
	}



	@Override
	public Page<userregionDto> getpageweather(String keyword, int page) {
		// TODO Auto-generated method stub
		Pageable pageable =PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.ASC,"areacode"));
		Page<WeatherregionEntity> jusodata=weatherhandler.getweatherregion(keyword,pageable);
		
		
			
		Page<userregionDto> juso=jusodata.map(m->
				userregionDto.builder().
				region(m.getstep1()+"  "+m.getstep2()+"  "+m.getstep3())
				.gridx(m.getgridx())
				.gridy(m.getgridy())
				.build()
				);
			
		
		System.out.println(juso.getTotalPages());
		System.out.println(juso.getContent());
		return juso;
	}
	//캐시삭제
	@Scheduled(fixedDelay = 1000*60*59)//59분마다삭제 //웨더못찾음..
	@CacheEvict(value = "getweather",beforeInvocation = false,allEntries = true) 
	//키값넣으면 특정 키값도 삭제가능
	public void cashwetherdelete() {
		//어노테이션으로삭제 비폴이노배케이션을실행해서 메서드실행이후에 삭제됨
		//올엔트리스는다삭제
		//밸류는뭘까 
		System.out.println("캐쉬삭제");
		Collection<String>cachenames=cachemanager.getCacheNames();
		for(String cachename:cachenames) {
			System.out.println("캐시네임:"+cachename);
		}
	}





	@Override
	public AreaRequestDto getCoordinate(String areacode) {
		// TODO Auto-generated method stub
		return null;
	}

}
