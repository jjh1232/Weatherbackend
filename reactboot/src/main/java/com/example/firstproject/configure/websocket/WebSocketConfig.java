package com.example.firstproject.configure.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.example.firstproject.Handler.Websocket.ChatHandler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSocket
@Configuration
public class WebSocketConfig implements WebSocketConfigurer{
	//웹소캣 콘피그 인터페이스적용
	private final ChatHandler chathandler;
	//근데 이것만이용하면 채팅방은하나뿐 stomp활용해야함
	@Override   
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// TODO Auto-generated method stub
		
		//사용할 핸들러랑 주소 cors허용주소설정 스톰프사용안하면 핸들러를구현해야함 ㅇㅇ
	registry.addHandler(chathandler,"open/ws").setAllowedOrigins("*").withSockJS();
	}

	//이거해줘야 적용이되더라 
		@Bean
	    public ServerEndpointExporter serverEndpointExporter() {
	        /*
	            2022.10.26[프뚜]:
	                Spring에서 Bean은 싱글톤으로 관리되지만,
	                @ServerEndpoint 클래스는 WebSocket이 생성될 때마다 인스턴스가 생성되고
	                JWA에 의해 관리되기 때문에 Spring의 @Autowired가 설정된 멤버들이 초기화 되지 않습니다.
	                연결해주고 초기화해주는 클래스가 필요합니다.
	         */
	        return new ServerEndpointExporter();
	    }
}
