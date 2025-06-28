package com.example.firstproject.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Handler.NoticeHandler;
import com.example.firstproject.Handler.NoticeLikehandler;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Utils.TestDataUtils;
import com.example.firstproject.configure.websocket.WebSocketConfig;

@SpringBootTest(properties = {
	    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
	})
//웹소켓테스트에서안된다함 
@ActiveProfiles("test")//
//@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration")
@Transactional //이거 테스트코드에안붙이면실제 db에 작동됨;
public class LikeTest {

	@Autowired
	private NoticeHandler handler;
	
	@Autowired
	private NoticeRepository noticerepo;
	@Autowired
	private MemberRepository memberrepo;
	
	//트랜잭션템플릿
	@Autowired
	private TransactionTemplate trtem;
	//레디스 db 테스트에선 목빈으로 레디스사용시 또 여기연결해서빈등록해서문제생김
	  @MockBean(name = "redisConnectionFactory")
	    private RedisConnectionFactory redisConnectionFactory;

	    @MockBean(name = "redisTemplate")
	    private RedisTemplate<String, Object> redisTemplate;

	    // 필요하다면 CacheManager도 Mock으로 대체
	     @MockBean(name = "redisCachemanager")
	     private CacheManager cacheManager;
	
	//주입문제떄매 부트테스트하면 웹소캣이 실행되는데 실제톰캣이사용안돼서 여러문제가생긴다 ;
	//아래처럼목객체를쓰거나
	//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) 이걸로하면 
	//톰캣도실행하나봄
	 @MockBean
	 private SimpMessageSendingOperations messagingTemplate;
	 
	@Autowired
	private ApplicationContext context;

	@Test
	public void checkWebSocketBeans() {
	    System.out.println("WebSocketConfig 빈: " + context.getBeanNamesForType(WebSocketConfig.class));
	    System.out.println("ServerEndpointExporter 빈: " + context.getBeanNamesForType(ServerEndpointExporter.class));
	}
	
	//트랜잭션테스트

	public void checktransaction() {
		boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
		System.out.println("트랜잭션"+txActive);
	}
	//@Test
	public void increaselikenum() throws InterruptedException {
		
		 System.out.println("좋아요증가테스트!");
		Long noticeid=115L; //이게비어있네지금
		int userCount=100;
		//테스트에서 여러스레드풀을만들어줌
		ExecutorService executorservice=Executors.newFixedThreadPool(userCount);
		
		CountDownLatch startlatch = new CountDownLatch(1);
		CountDownLatch readyLatch=new CountDownLatch(userCount);
		NoticeEntity notice=TestDataUtils.createTestnotice(noticerepo,noticeid);
		 System.out.println("노티스생성");
		for (int i=0;i<userCount;i++) {
			Long userid=i+100L;
			//정의된 스레드에 작업을 할당함
			executorservice.submit(()->{
				try {
					
					readyLatch.countDown(); //준비완료
					startlatch.await() ; //시작신호대기
					//이거 스레드풀안에서하면 외부 트랜잭션으로돌아서 해당클래스내부에 트랜잭션을걸든 트랜잭션템플릿주입하든
					//해서 이걸로실행해야함
					trtem.executeWithoutResult(status->{
						//System.out.println("트랜잭션 활성화: " + TransactionSynchronizationManager.isActualTransactionActive());
						MemberEntity member=TestDataUtils.createTestuser(memberrepo, userid);
						FavoriteEntity entity=new FavoriteEntity(userid, member,notice);
						handler.favoritesave(entity);
						throw new RuntimeException("강제 롤백 테스트");
					});
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					//latch.countDown();
				}
			});
		}
		   readyLatch.await(); // 모든 스레드가 준비될 때까지 대기
	        startlatch.countDown(); // 모든 스레드 동시 시작
	        
		executorservice.shutdown();
		executorservice.awaitTermination(5,TimeUnit.SECONDS);
		//갯수확인좋아요
		long likecount=handler.likecounts(notice.getNoticeid());
		assertEquals(userCount,likecount,"동시성이면 다름둘이");
	}
}
