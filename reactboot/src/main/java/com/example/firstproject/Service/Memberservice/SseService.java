package com.example.firstproject.Service.Memberservice;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.Notification;
import com.example.firstproject.Repository.EmitterRepository;
import com.example.firstproject.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SseService {

	
	private final EmitterRepository emitterRepository;
	
	private final NotificationRepository notificationrepository;
	
	 //====================SSE subscribe========================
	
	
		public SseEmitter SSEcon(Long userid) {
			// TODO Auto-generated method stub
			//새로운 SseEmitter만든다
			SseEmitter sseEmitter=new SseEmitter(360000000*100*60L); //타임아웃시간넣어줄수있음
			
			//유저아이디로 SseEmitter를저장한다
			emitterRepository.save(userid,sseEmitter);
			
			//세션이 종료될 경우 저장한 sseemitter를삭제한다
			sseEmitter.onCompletion(()->emitterRepository.delete(userid));
			sseEmitter.onTimeout(()->emitterRepository.delete(userid));
			//sseEmitter.onError((e)-> SSEController.sseEmitters.remove(userid));
			//503 service unavailable오류가 발생하지 않도록 첫데이터를 보내야함!
			//연결
			try {
				System.out.println("더미메세지");
				sseEmitter.send(SseEmitter.event().id("").name("connect").data("connection complate"));
			}
			catch(IOException exception) {
				System.out.println("에러");
				 //throw new ApplicationException(ErrorCode.NOTIFICATION_CONNECTION_ERROR);
			}
			
			return sseEmitter;
		}

		
	//수신자에게 채팅 수신알림
		public void sendtonotice(MemberEntity tomember, Long userId,Long noticeid,String noticetitle) {
	        // 유저 ID로 SseEmitter를 찾아 이벤트를 발생 시킨다.
			if(!emitterRepository.get(tomember.getId()).isPresent()) { //널일경우 false를반환ㅇ하니까반대로!
				System.out.println("현재 해당유저는 접속중이지않습니다!");
				 Notification notification=Notification.builder()
	                		.noticeid(noticeid)
	                		.message("회원님의 "+noticeid+"번글" +noticetitle +"에 새로운 댓글이 달렸습니다!")
	                		.member(tomember)
	                		.build();
	                //db저장
	                tomember.addnotifications(notification);
	                notificationrepository.save(notification);
	                System.out.println("db에만저장!");
			}
			else {
			System.out.println("해당유저 접속중 send시작");
			
			emitterRepository.getemitteruser();
			
			
			/*
			SseEmitter emitter=emitterRepository.exget(notificationId);
			
			try {
				emitter.send(SseEmitter.event()
						.id(userId.toString())
						.name("ms")
						.data("알림이왔습니다"));
						
					
				
			}catch (IOException exception){
				
			}*/
			System.out.println("노티스작성id:"+tomember.getId());
			System.out.println("댓글작성자id:"+userId);
			System.out.println("작성글번호id:"+noticeid);
			System.out.println("글제목:"+noticetitle);
			
			
	        emitterRepository.get(tomember.getId()).ifPresentOrElse(sseEmitter -> {
	            try {
	                sseEmitter.send(
	                		SseEmitter.event(). 
	                		id(userId.toString())//해당이벤트의 아이디설정
	                		.name("message").//해당이벤트의이름설정
	                		data("회원님의 "+noticeid+"번글" +noticetitle +"에 새로운 댓글이 달렸습니다!"));//
	                
	                Notification notification=Notification.builder()
	                		.noticeid(noticeid)
	                		.message("회원님의 "+noticeid+"번글" +noticetitle +"에 새로운 댓글이 달렸습니다!")
	                		.member(tomember)
	                		.build();
	                //db저장
	                tomember.addnotifications(notification);
	                notificationrepository.save(notification);
	                System.out.println("보내긴했는디");
	            }
	            catch (IOException exception) {
	        // IOException이 발생하면 저장된 SseEmitter를 삭제하고 예외를 발생시킨다.
	                emitterRepository.delete(tomember.getId());
	                System.out.println("에러나서삭제햇음");
	                //throw new ApplicationException(ErrorCode.NOTIFICATION_CONNECTION_ERROR);
	            }
	        }, () -> System.out.println("No emitter found"));
	        
			}
	    }
		
		public void sendtocomment(MemberEntity tomember,Long userid,Long noticeid,String noticetitle) {
			//data에 map과 put으로 맵핑해서 넘길수도있음!
			System.out.println(emitterRepository.get(tomember.getId()));
			if(!emitterRepository.get(tomember.getId()).isPresent()) {
				System.out.println("현재 해당유저는 접속중이지않습니다!");
				 Notification notification=Notification.builder()
	                		.noticeid(noticeid)
	                		.message("회원님이" +noticetitle +"에 작성한 댓글에 새로운 대댓글이 달렸습니다!")
	                		.member(tomember)
	                		.build();
	                
	                tomember.addnotifications(notification);
	                
	                notificationrepository.save(notification);
	                System.out.println("db에만저장!");
			}
			else {
			 emitterRepository.get(tomember.getId()).ifPresentOrElse(sseEmitter -> {
		            try {
		                sseEmitter.send(
		                		SseEmitter.event(). 
		                		id(userid.toString())//해당이벤트의 아이디설정
		                		.name("message").//해당이벤트의이름설정
		                		data("회원님이" +noticetitle +"에 작성한 댓글에 새로운 대댓글이 달렸습니다!"));//
		                
		                Notification notification=Notification.builder()
		                		.noticeid(noticeid)
		                		.message("회원님이" +noticetitle +"에 작성한 댓글에 새로운 대댓글이 달렸습니다!")
		                		.member(tomember)
		                		.build();
		                
		                tomember.addnotifications(notification);
		                
		                notificationrepository.save(notification);
		                System.out.println("보내긴했는디");
		            }
		            catch (IOException exception) {
		        // IOException이 발생하면 저장된 SseEmitter를 삭제하고 예외를 발생시킨다.
		                emitterRepository.delete(tomember.getId());
		                System.out.println("에러나서삭제햇음");
		                //throw new ApplicationException(ErrorCode.NOTIFICATION_CONNECTION_ERROR);
		            }
		        }, () -> System.out.println("No emitter found"));
		        
		}
		}
		/*
		//댓글알림-게시글 작성자에게
		public void notifycomment(Long postId) {
			Post post = postRepository.findById(postId).orElseThrow(
	                () -> new IllegalArgumentException("게시글을 찾을 수 없습니다.")
	        );

	        Long userId = post.getUser().getId();

	        if (NotificationController.sseEmitters.containsKey(userId)) {
	            SseEmitter sseEmitter = NotificationController.sseEmitters.get(userId);
	            try {
	                sseEmitter.send(SseEmitter.event().name("addComment").data("댓글이 달렸습니다."));
	            } catch (Exception e) {
	                NotificationController.sseEmitters.remove(userId);
	            }
	        }
	    }
	    }
	    */
}
