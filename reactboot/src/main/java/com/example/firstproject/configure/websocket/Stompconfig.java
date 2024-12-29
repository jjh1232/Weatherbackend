package com.example.firstproject.configure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.example.firstproject.configure.Interceptor.StompHandler;
import com.example.firstproject.configure.Interceptor.Stompinterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSocketMessageBroker//웹소캣의 메세지브로커기능을 활성화하는 어노테이션 이를통해 메시지핸들링과 브로커구성이가능해짐
public class Stompconfig implements WebSocketMessageBrokerConfigurer{
	
	private final Stompinterceptor cepter;
	
	private final StompHandler stomphandler;
	
	private final ChatErrorHandler errorhandler;
	@Override
		public void registerStompEndpoints(StompEndpointRegistry registry) {
			// TODO Auto-generated method stub
			registry.addEndpoint("/open/stomp")
			.setAllowedOriginPatterns("*") //전역허용
			//.setAllowedOrigins("*")
			.addInterceptors(cepter)//인터셉터추가 
			.withSockJS() //참고로 웹이아닌 앱에서 사용하면 작동하지 않는다고함 
			
			;
			registry.setErrorHandler(errorhandler);//에러핸들러!
		}
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// TODO Auto-generated method stub
		registry.enableSimpleBroker("/sub","/queue");//1대다,1대1둘다설정가능
		registry.setApplicationDestinationPrefixes("/pub");
		
	}
	
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		//클라이언트의 인바운드 채널에 대한 설정을 구ㅎ성하는 메서드이다 웹소캣 연결이전에 처리작업을 수행할수있다
		// TODO Auto-generated method stub
		registration.interceptors(stomphandler); //이것도인터셉트추가
	}

}
