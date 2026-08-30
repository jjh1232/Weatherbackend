package com.example.firstproject.Service.Memberservice;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.firstproject.Dto.userdataDto.NotifiResult;
import com.example.firstproject.Dto.userdataDto.NotificationDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.Notification;
import com.example.firstproject.Repository.EmitterRepository;
import com.example.firstproject.Repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class SseService {

	
	//Emitter 자체 타임아웃. 이 시간이 지나면 브라우저가 알아서 다시 연결한다.
	private static final long TIMEOUTMS = 60L * 60 * 1000;      //1시간
	//유휴 연결이 프록시에 끊기지 않게 보내는 신호 주기.
		private static final long HEARTBEATMS = 30L * 1000;         //30초
	//알림 문구에 넣을 제목 길이 상한
	private static final int TITLEMAXLEN = 20;

	private final EmitterRepository emitterRepository;
	
	private final NotificationRepository notificationrepository;
	
	 //====================SSE subscribe========================
	
	//첫커넥트
		public SseEmitter SSEcon(Long userid) {

			//연결마다 고유한 키를 준다. 탭을 여러 개 열어도 서로 덮어쓰지 않는다.
			String key = emitterRepository.makekey(userid);
			SseEmitter sseEmitter = new SseEmitter(TIMEOUTMS);
			emitterRepository.save(key, sseEmitter);

			//끊긴 연결은 반드시 치워야 한다. 남아 있으면 "접속 중"으로 잘못 판단해서
			//알림을 DB 에 저장하지 않고 허공에 보내게 된다.
			sseEmitter.onCompletion(() -> emitterRepository.deletebykey(key));
			sseEmitter.onTimeout(() -> {
				emitterRepository.deletebykey(key);
				sseEmitter.complete();
			});
			//예전에는 이 줄이 주석 처리돼 있어서, 네트워크가 끊기면 연결이 영원히 남았다.
			sseEmitter.onError(e -> emitterRepository.deletebykey(key));

			//첫 데이터를 바로 보내지 않으면 503 이 날 수 있다.
			try {
				sseEmitter.send(SseEmitter.event().id(key).name("connect").data("connected"));

				Long unreadcount = notificationrepository.unreadnotificount(userid);
				sseEmitter.send(SseEmitter.event().id(key).name("unreadcount").data(unreadcount));
			} catch (IOException exception) {
				log.info("SSE 최초 전송 실패 key={}", key);
				emitterRepository.deletebykey(key);
			}

			return sseEmitter;
		}

		/**
		 * 이 유저의 모든 연결(탭)에 이벤트를 보낸다.
		 * 실패한 연결은 그 자리에서 정리한다.
		 */
		private void sendtouser(Long userid, String eventname, String data) {
			Map<String, SseEmitter> emitters = emitterRepository.findallbyuserid(userid);
			if (emitters.isEmpty()) {
				log.info("접속 중이 아님 userid={} (DB 알림만 남는다)", userid);
				return;
			}
			emitters.forEach((key, emitter) -> {
				try {
					emitter.send(SseEmitter.event().id(key).name(eventname).data(data));
				} catch (IOException exception) {
					log.info("SSE 전송 실패, 연결 정리 key={}", key);
					emitterRepository.deletebykey(key);
				}
			});
		}

		/**
		 * 알림을 DB 에 남긴다.
		 * <b>전송보다 먼저</b> 해야 한다. 예전에는 send() 성공 뒤에 저장해서,
		 * 전송이 실패하면 알림이 DB 에도 안 남고 그대로 사라졌다.
		 */
				//주의: 여기에 @Transactional 을 붙여도 소용없다.
		//스프링 AOP 는 public 메서드만 프록시로 감싸고, 같은 클래스 안에서 부르면
		//프록시를 거치지 않는다. 그래서 트랜잭션은 호출하는 public 쪽에 건다.
		private Notification savenotification(MemberEntity tomember, Long noticeid, String message) {
			Notification notification = Notification.builder()
					.noticeid(noticeid)
					.message(message)
					.member(tomember)
					.build();
			tomember.addnotifications(notification);
			return notificationrepository.save(notification);
		}

		/**
		 * 유휴 연결이 프록시에 끊기지 않도록 주기적으로 신호를 보낸다.
		 * 클라우드플레어 같은 앞단은 보통 100초쯤 조용하면 연결을 끊는다.
		 * Emitter 타임아웃(1시간)만 믿으면 배포 후에 1~2분마다 끊긴다.
		 */
		@Scheduled(fixedDelay = HEARTBEATMS)
		public void heartbeat() {
			Map<String, SseEmitter> all = emitterRepository.findall();
			if (all.isEmpty()) {
				return;
			}
			all.forEach((key, emitter) -> {
				try {
					//주석 이벤트라 클라이언트의 onmessage 를 건드리지 않는다.
					emitter.send(SseEmitter.event().comment("heartbeat"));
				} catch (Exception exception) {
					emitterRepository.deletebykey(key);
				}
			});
		}

	//수신자에게 채팅 수신알림
		@Transactional
		public void sendtonotice(MemberEntity tomember, Long userId, Long noticeid, String noticetitle) {

			String message = shorttitle(noticeid, noticetitle) + "에 새로운 댓글이 달렸습니다.";

			//1) 먼저 남긴다. 접속 중이든 아니든 알림 목록에는 반드시 보여야 한다.
			savenotification(tomember, noticeid, message);

			//2) 접속 중이면 실시간으로도 띄운다. 실패해도 1)은 이미 끝났다.
			sendtouser(tomember.getId(), "noticealarm", message);
		}

		//수신자에게 대댓글 알림
		@Transactional
		public void sendtocomment(MemberEntity tomember, Long userid, Long noticeid, String noticetitle) {

			String message = shorttitle(noticeid, noticetitle) + "에 남긴 댓글에 답글이 달렸습니다.";

			savenotification(tomember, noticeid, message);
			sendtouser(tomember.getId(), "commentalarm", message);
		}

		/**
		 * 알림 문구에 들어갈 글 제목.
		 * 제목을 통째로 넣으면 알림 한 줄이 네 줄이 된다. 20자에서 자른다.
		 * 제목이 없는 글은 글번호로 대신한다.
		 */
		private String shorttitle(Long noticeid, String title) {
			if (title == null || title.trim().isEmpty()) {
				return noticeid + "번 글";
			}
			String trimmed = title.trim();
			if (trimmed.length() > TITLEMAXLEN) {
				trimmed = trimmed.substring(0, TITLEMAXLEN) + "…";
			}
			return "「" + trimmed + "」";
		}

		public NotifiResult<NotificationDto> getusernotifi(Long memberid,int page) {
			//페이지로 10개씩가져오자
			System.out.println("서비스시작");
			//DEFAULT_DIRECTION 은 ASC 라 가장 오래된 알림이 1페이지에 나왔다.
			//게다가 createdDate 는 문자열이라(초가 한 자리) 같은 분 안에서 순서가 뒤집힌다.
			//증가하는 식별자로 내림차순 정렬하는 게 정확하고 빠르다.
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.Direction.DESC,"id"));
			System.out.println("서비페이지리퀘스트");
			List<Notification> notifi=notificationrepository.findByMemberId(memberid,pageable);
			System.out.println("서비스데이터가져오기");
			//리스트는내장된 map메소드가페이지와다르게없다 스트림을사용
			List<NotificationDto> dtolist=notifi.stream().map(m->NotificationDto.builder()
														.id(m.getId())
														.message(m.getMessage())
														.red(m.getCreatedDate())
														.noticeid(m.getNoticeid())
														.isread(m.isReading())
														.build())
														.toList();
			//페이지객체보다따로가더좋다네..
			Long totalcount=notificationrepository.notificount(memberid);
			int totalpages=(int) Math.ceil((double) totalcount/10);
			return new NotifiResult<>(dtolist, page, totalpages, totalcount);
			
		}
				//배지에 쓰는 수. "안 읽은" 알림만 센다.
		//예전에는 notificount(전체 개수)를 돌려줘서, 읽었든 말든 누적 개수가 그대로 떴다.
		public Long notificationcount(Long userid) {
			return notificationrepository.unreadnotificount(userid);
		}
		
		//모두읽기처리
		@Transactional //트랜잭션필수라고함;벌크쿼리할떄
		public void readallnotify(Long memberid) {
			LocalDateTime current=LocalDateTime.now();
			//시간 넣기 포맷방식떄매 이상해서 없앳음 필요없긴할듯
			notificationrepository.notifireadall(memberid);
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
		public void deleteemiter(Long userid) {
			log.info("로그아웃 - SSE 연결 정리 userid={}", userid);
			//complete() 까지 불러야 브라우저 쪽 EventSource 도 같이 닫힌다.
			//서버 맵에서만 지우면 클라이언트는 계속 붙어 있으려 한다.
			emitterRepository.deleteallbyuserid(userid);
		}
}
