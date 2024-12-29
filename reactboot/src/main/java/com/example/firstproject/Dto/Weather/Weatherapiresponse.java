package com.example.firstproject.Dto.Weather;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class Weatherapiresponse {

	private Response response;
	
	@Data
	public static class Response{
	private WeatherHeader header;
	
	private WeatherBody body;
	
	
	@Data
	public static class WeatherHeader{
		private String resultCode;
		private String resultMsg;
	}
	
	@Data
	public static class WeatherBody{
		private String dataType;
		
		private Weatheritems items;
		
		private int numOfRows;
		private int pageNo;
		
		
		private int totalCount;
		
		@Data
		public static class Weatheritems{
			
			private List<Weatheritem> item;
			
			
			@Data
			@Getter
			public static class Weatheritem{
				
				private String baseDate;
				
				private String baseTime;
				
				private String category;
				private String fcstDate;
				private String fcstTime;
				private String fcstValue;
				private String nx;
				
				private String ny;
				
			}
		
		}
	}
	}
}
