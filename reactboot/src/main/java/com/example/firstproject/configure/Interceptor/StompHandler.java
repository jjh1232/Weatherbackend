package com.example.firstproject.configure.Interceptor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.MessageBuilder;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.UserChatrecord.LastReadId;
import com.example.firstproject.Entity.UserChatrecord.chatrecord;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.roomrepo.LastchatreadRepository;
import com.example.firstproject.Service.JwtService;
import com.example.firstproject.configure.PrincipalDetails;
import com.example.firstproject.configure.websocket.StompPrincipal;

import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Configuration //필요하면 @Order로 우선순위 설정 컴포넌트박앗는데아니길래 
@Slf4j
@Profile("!test")
public class StompHandler implements ChannelInterceptor{ //채널인섭셉터 인터페이스 

	//채널인터셉터가 편하다 jwt인증에는
	//연결종료는 여기서 할수도있지만 @eventlistener에서 하는게좋다함
	private final JwtService jwtservice;
	
	private final MemberRepository memberrepo;
	
	//일단 메모리에저장
	//해제시 유저아이디를 찾을수가없다
	private final Map<Long,Set<String>> connectusers=new ConcurrentHashMap<>();
	private final Map<String ,Long> sessiontouserid=new ConcurrentHashMap<>();
	//레디스로저장하자 익숙해질겸
	//직접만든 빈을 명시해서 사용해야함 뭐 기존에있는거랑 겹치나봄
	//이거 롬북final쓰면 의미가 없대 lombok.config를 설정해야함
	@Qualifier("redisTemplateString")
	private final RedisTemplate<String, String> redistemplate;
	
	
	//라스트챗리포지토리
	private final LastchatreadRepository chatreadrepo;
	//websocket을 통해 들어온 요청이 처리되기전에 실행됨 
	@Override  
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			// TODO Auto-generated method stub
		//메시지의 stomp헤더를 쉽게 접근하기위한 유틸리티 클래스임 
		//웹소캣을 통해 들어온 요청의 stomp헤더를 가로채어 헤더정보를 가져올수있음
	log.info("핸들러");
		StompHeaderAccessor accessor=StompHeaderAccessor.wrap(message);
		//아래에 웹소캣 연결시 헤더의 토큰 유효성을 검증하면된다 
		
		System.out.println("프리센드채널"+channel);
		System.out.println("프리센드메세지"+message);
		String userid=null;
		String sessionid=accessor.getSessionId(); //세션아이디도
		if(accessor.getCommand().equals(StompCommand.CONNECT)) {
			//메세지의 구독명령이 connect인경우에만실행 이거 아니면 구독할때나 보낼떄나 다걸림
			//메세지에들어가서 이걸로 꺼내야함
			System.out.println("커넥트단계에서인증");
		String accessheader=accessor.getFirstNativeHeader("Authorization");
		log.info("엑세스헤더"+accessheader);
		String Refreshheader=accessor.getFirstNativeHeader("Refreshtoken");
		log.info("리프레쉬헤더"+Refreshheader);
		String strroomid=accessor.getFirstNativeHeader("roomid");
		String roomid=strroomid;
			try {
				//요거
				jwtservice.tokenvalid(accessheader);//헤더정보 검증 널인지 Bearer이있는지
				//일단에러는 저서비스내에서하는데 그럼진행이되나?
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//아니면 jwtservice에서
			
			//토큰기간
			//bearer 을 뺴야하나? 근데여기서 트루펄스 값이 무슨상관이지 ;
			//Bearer을 안때서 검증자체가안되서 거기서에러난듯? 근데거기서나니까 연결 종료됨 
			
			String accesstoken=accessheader.replace("Bearer ", "");
			if(jwtservice.checktokenvalid(accesstoken)) {
				System.out.println("액세스토큰정상");
				//사용자정보추출
				String username=jwtservice.gettokenclaim(accesstoken);
				MemberEntity User=memberrepo.findByUsername(username).orElseThrow();
				
				//프린시펄
				PrincipalDetails principal=new PrincipalDetails(User);
				Authentication authentication=new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				
				userid=User.getId().toString();
				//sessionid=accessor.getSessionId();
				
				
				
				
			}
			else {//중복코드방지위해서
				System.out.println("액세스토큰만료");
				
				//===========================리프레쉬확인
				try {
					//리프레쉬 헤더체크 에러시익셉션일어남 Bearer여부체크임
					jwtservice.tokenvalid(Refreshheader);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					String refreshtoken=Refreshheader.replace("Bearer ", "");
					//리프레쉬토큰기간체크 
					if(jwtservice.checktokenvalid(refreshtoken)) {
						System.out.println("토큰합격");
						MemberEntity member=memberrepo.findByrefreshtoken(refreshtoken).orElseThrow();
						//인증 
						PrincipalDetails principal=new PrincipalDetails(member);
						Authentication authentication=new UsernamePasswordAuthenticationToken(principal,null, principal.getAuthorities());
						SecurityContextHolder.getContext().setAuthentication(authentication);
						userid=member.getId().toString();
					//	sessionid=accessor.getSessionId();
					}
						//else문 return 메세지 중복체크후에하려고
					else {
						System.out.println("리프레쉬 토큰기간이지났습니다 재로그인필요");
						return null; //null을 리턴하면 문제가없음		
					}
			}//엘스문 끝 인증성공후 중복체크로직
			//유저아이디가 없을시 생성 있을시 값리턴
			/*======================기존그냥맵에저장==============
			connectusers.putIfAbsent(userid,new HashSet<>());
			//해쉬셋으로저장이유는 모바일이나 컴퓨터 중복때문이라함
			connectusers.get(userid).add(sessionid);
			sessiontouserid.put(sessionid,userid);
		
			 System.out.println("현재유저수:" + connectusers.size());
		*/
			
			redistemplate.opsForSet().add("stomp:chatroom:"+roomid+":userid:"+userid, sessionid);
			redistemplate.opsForValue().set("stomp:sessiontouser:"+sessionid,userid);
			redistemplate.opsForValue().set("stomp:sessiontoroom:"+sessionid,roomid);
			//이후 사용자의 유저아이디가필요해서 
			accessor.setUser(new StompPrincipal(userid));
			
			return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
				
				
			
			
		}//첫이프문 커넥트여부
		//이벤트리스너가안됨..
		 if(accessor.getCommand().equals(StompCommand.DISCONNECT)) {
			 System.out.println("디스커넥트");
				String deletesessionid=accessor.getSessionId();
				//세션을 받고 레디스에저장한 해당세션의 유저아이디와 룸아이디를 받음
				String userkey="stomp:sessiontouser:"+deletesessionid;
				String roomkey="stomp:sessiontoroom:"+deletesessionid;
				
				System.out.println("유저키확인:"+userkey);
				// 해당키로 유저아이디와 룸아이디받음 유저아디는Long으로저장했네;
				//에러처리해야한다
				String deleteuserid= (String) redistemplate.opsForValue().get(userkey);
				if (deleteuserid == null) {
				    log.warn("세션 종료 중 userId 없음: {}", userkey);
				    return  null;
				}
				String deleteroomid= (String) redistemplate.opsForValue().get(roomkey);
				if (deleteroomid == null) {
				    log.warn("세션 종료 중 roomId 없음: {}", roomkey);
				    return null;
				}
				String lastchatkey="stomp:chat:lastread:roomid:"+deleteroomid+":userid:"+deleteuserid;
				
				String lastchatid= (String) redistemplate.opsForValue().get(lastchatkey);
				if (lastchatid == null) {
				    log.warn("세션 종료 중 lastChatId 없음: {}", lastchatkey);
				    return null;
				}
		 
				//long변환
				Long saveloginid=Long.valueOf(deleteuserid);
				Long saveroomid=Long.valueOf(deleteroomid);
				Long savelastchat=Long.valueOf(lastchatid);
				
			
				LastReadId id=LastReadId.builder().userid(saveloginid).roomid(saveroomid).build();
				chatrecord lastchat=chatrecord.builder().id(id).lastchatid(savelastchat).build();
				//유저체팅저장
				chatreadrepo.save(lastchat);
				
				//유저키가존재하면 레디스유저키삭제
				if(deleteuserid !=null) {
					redistemplate.opsForSet().remove("stomp:chatroom:"+deleteroomid+":userid:"+deleteuserid, deletesessionid);
					redistemplate.delete(userkey);//유저키삭제
					redistemplate.delete(roomkey);//유저키삭제
					redistemplate.delete(lastchatkey);//유저마지막채팅갱신삭제
					//사이즈가 0일떄 키자체도삭제
					if(redistemplate.opsForSet().size("stomp:chatroom:"+deleteroomid+":userid:"+deleteuserid)==0) {
						redistemplate.delete("stomp:useridtosession:"+deleteuserid);
					}
				}else {
					System.out.println("해당id못찾음:"+deletesessionid);
				}
				System.out.println("연결종료 id:"+deletesessionid);
			 	/*
			 System.out.println("디스커넥트");
				String deleteid= accessor.getSessionId();
				 System.out.println("디스커넥트세션아이디"+deleteid);
				Long deleteuserid=sessiontouserid.get(deleteid);
				System.out.println("딜리트할유저아이디"+deleteuserid);
				Set<String> sessions=connectusers.get(deleteuserid);
				if(sessions !=null) {
					//이거 참조가 꺼내오면 같은곳을참조해서 실제데이터도삭제된다함
					 System.out.println("세션삭제");
					sessions.remove(deleteid);
					sessiontouserid.remove(deleteid);
					if(sessions.isEmpty()) {
						connectusers.remove(deleteuserid);
					}
				}
				*/
			 }
			 
		System.out.println("커넥트가아님");
		System.out.println("무슨요청?:"+accessor.getCommand()+"//세션아이디:"+sessionid);
		//셋유저쓸려먼 모든 곳에서 principal세팅필요함
		String useridobj=(String) redistemplate.opsForValue().get("stomp:sessiontouser:"+sessionid);
		if(useridobj !=null) {
			String loginid =useridobj;
			System.out.println("유저로그인아이디:"+loginid);
			accessor.setUser(new StompPrincipal(loginid));
		}
		
		//이걸로보내야 setuser같은 헤더도 다된다고함;
		return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
		
		}
	
	@Override
		public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
			// TODO Auto-generated method stub
			
		//창닫기나 강제종료시 이벤트리스너를못쓰면 여기에서 또처리해야한다
		StompHeaderAccessor accessor=StompHeaderAccessor.wrap(message);
		//아래에 웹소캣 연결시 헤더의 토큰 유효성을 검증하면된다 
		
		 System.out.println("postsend실행");
		
		 if(accessor.getCommand().equals(StompCommand.DISCONNECT)) {
			 System.out.println("디스커넥트");
				String deletesessionid=accessor.getSessionId();
				//세션을 받고 레디스에저장한 해당세션의 유저아이디와 룸아이디를 받음
				String userkey="stomp:sessiontouser:"+deletesessionid;
				String roomkey="stomp:sessiontoroom:"+deletesessionid;
				
				System.out.println("유저키확인:"+userkey);
				// 해당키로 유저아이디와 룸아이디받음 유저아디는Long으로저장했네;
				//방어코드를해야 에러가 줄고 웹소캣특성상여러번실행되서 오류방지
				String deleteuserid= (String) redistemplate.opsForValue().get(userkey);
				if (deleteuserid == null) {
				    log.warn("세션 종료 중 userId 없음: {}", userkey);
				    return;
				}
				String deleteroomid= (String) redistemplate.opsForValue().get(roomkey);
				if (deleteroomid == null) {
				    log.warn("세션 종료 중 roomId 없음: {}", roomkey);
				    return;
				}
				String lastchatkey="stomp:chat:lastread:roomid:"+deleteroomid+":userid:"+deleteuserid;
				
				String lastchatid= (String) redistemplate.opsForValue().get(lastchatkey);
				if (lastchatid == null) {
				    log.warn("세션 종료 중 lastChatId 없음: {}", lastchatkey);
				    return;
				}
				
				//long변환
				Long saveloginid=Long.valueOf(deleteuserid);
				Long saveroomid=Long.valueOf(deleteroomid);
				Long savelastchat=Long.valueOf(lastchatid);
				
			
				LastReadId id=LastReadId.builder().userid(saveloginid).roomid(saveroomid).build();
				chatrecord lastchat=chatrecord.builder().id(id).lastchatid(savelastchat).build();
				//유저체팅저장
				chatreadrepo.save(lastchat);
				
				//유저키가존재하면 레디스유저키삭제
				if(deleteuserid !=null) {
					redistemplate.opsForSet().remove("stomp:chatroom:"+deleteroomid+":userid:"+deleteuserid, deletesessionid);
					redistemplate.delete(userkey);//유저키삭제
					redistemplate.delete(roomkey);//유저키삭제
					redistemplate.delete(lastchatkey);//유저마지막채팅갱신삭제
					//사이즈가 0일떄 키자체도삭제
					if(redistemplate.opsForSet().size("stomp:chatroom:"+deleteroomid+":userid:"+deleteuserid)==0) {
						redistemplate.delete("stomp:useridtosession:"+deleteuserid);
					}
				}else {
					System.out.println("해당id못찾음:"+deletesessionid);
				}
				System.out.println("연결종료 id:"+deletesessionid);
		
			 }
		}
				
}
