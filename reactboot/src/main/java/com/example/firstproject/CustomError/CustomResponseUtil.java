package com.example.firstproject.CustomError;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomResponseUtil {

	 public static void unAuthentication(HttpServletResponse response, String msg) {
		 //로그인 에러처리용 커스텀 형식 
	        try {
	            ObjectMapper om = new ObjectMapper();
	            ResponseDto<?> responseDto = new ResponseDto<>(-1, msg, null);//코드 메세지 데이터
	            String responseBody = om.writeValueAsString(responseDto);//오브젝트맵퍼로 json형식
	            response.setContentType("application/json; charset=utf-8");
	            response.setStatus(401);
	            response.getWriter().println(responseBody);
	        } catch (Exception e) {
	            System.out.println("서버 파싱 에러");
	        }
	    }
}
