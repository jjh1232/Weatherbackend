package com.example.firstproject.configure.Interceptor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Component
public class Stompinterceptor implements HandshakeInterceptor{

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		// TODO Auto-generated method stub
		
		System.out.println("핸드쉐이크시작인터셉터on");
		System.out.println("리퀘"+request.getHeaders().getFirst("Authorization"));
		System.out.println("리퀘"+request.getHeaders().getFirstDate("Authorization"));
		
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
		System.out.println("핸드쉐이크완료인터셉터on");
		// TODO Auto-generated method stub
		
	}

}
