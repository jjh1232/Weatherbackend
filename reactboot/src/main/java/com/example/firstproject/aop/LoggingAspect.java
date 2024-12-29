package com.example.firstproject.aop;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.extern.slf4j.Slf4j;

@Aspect //advide+pointcut 어드바이스는 부가기능모듈설정 포인트컷은 어드바이스적용할조인포인트선별
@Component
@Slf4j
public class LoggingAspect {
	
	//포인트컷으로 사용할메소드 
	@Pointcut("execution(* com.example.firstproject..*.*(..))")
	public void all(){
		//아마여긴실행시간 모든 메서드에 적용할려함
		//근데이거 bcycreept 빈이생성이안되서 실행을못시킴..
	}
	
	@Pointcut("execution(* com.example.firstproject.controller..*.*(..))")
	public void controller() {
		//컨트롤러에 적용 
	}
	/*
	@Before	대상 “메서드”가 실행되기 전에 Advice를 실행합니다.
	@After	대상 “메서드”가 실행된 후에 Advice를 실행합니다.
	@AfterReturning	대상 “메서드”가 정상적으로 실행되고 반환된 후에 Advice를 실행합니다.
	@AfterThrowing	대상 “메서드에서 예외가 발생”했을 때 Advice를 실행합니다.
	@Around	대상 “메서드” 실행 전, 후 또는 예외 발생 시에 Advice를 실행합니다.

	@EnableAspectJAutoProxy어노테이션을원래 부트실행에서 붙여야 aop를인식하고 사용하지만
	부트는 알아서 적지않아도 프레임워크 사용가능함 

*/
	
	@Around("controller()")//컨트롤러로 포인트컷지정
	public Object times(ProceedingJoinPoint joinpoint) throws Throwable {
		long start=System.currentTimeMillis();
		
		
		try {
			//
			Object result = joinpoint.proceed();
			//프로시드는 들어온요청을 controller로 보낸다
			//이후return 으로 controller에서 처리된 요청을 반환함
			
			//signature에는 클라이언트가 호출한 메서드의 시그니처(리턴타입,이름,매게변수)정보가담겨있다
			 
			//오브젝트를 리설트를 리턴해야 다음으로넘어가짐 
			return result;
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			long end=System.currentTimeMillis();
			long timein=end-start;
			
			log.info("조인포인트시그네이처:{} 걸린시간:{}ms",joinpoint.getSignature(),timein);
		}
		
		return start;
		
		
		
	
	}
	
	
	 //스톰프컨트롤러 같은건 노로깅을해야함 콘트롤러에서시작된애가 핸들러까지영향을미침 ;
	//근데 request객체가없기때문에 에러가나버림
@Before("execution(* com.example.firstproject.controller..*.*(..))&& !@annotation(com.example.firstproject.aop.Logoutano)"
		+ "&&!@annotation(com.example.firstproject.aop.NoLogging)")
public void beforeloging(JoinPoint joinpoint) {
	log.info("aop로그시작"+joinpoint);
	
	//httpservletrequest를 이방법으로 받으면 귀찮지않게 사용가능하다 
	HttpServletRequest request =
	((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
	
	log.info("before에서 요청uri:"+request.getRequestURI());
	
	
	log.info("before에서 요청로케일정보:"+request.getLocale());
	String userip=getClientIP(request);
	log.info("before에서 요청ip주소:"+userip);
	//이방식으로 하면 ip가프록시나따른거때매 이상하게나옴 설정검색
	
	//응답정보는 리스폰스 또한 before이아니라 after에서해야할듯? 아니면프로시드나 
	
	//오브젝트형식이라 정리해줘야함
	/* 이방법은컨트롤러에 httpservletrequest를 계속 변수로 받아야 사용가능함;
	for(Object obj:joinpoint.getArgs()) {
		//instanceof 객체타입이 해당클래스인지 확인할수있음
		if(obj instanceof HttpServletRequest ) {
		HttpServletRequest req=(HttpServletRequest) obj;
		log.info(req.toString());
		}
		}
		*/
	
	
	
	
	
}

//아이피구하는표준스태틱
public static String getClientIP(HttpServletRequest request) {
	
    String ip = request.getHeader("X-Forwarded-For");
    log.info("X-FORWARDED-FOR : " + ip);

    if (ip == null) {
        ip = request.getHeader("Proxy-Client-IP");
        log.info("Proxy-Client-IP : " + ip);
    }
    if (ip == null) {
        ip = request.getHeader("WL-Proxy-Client-IP");
        log.info("WL-Proxy-Client-IP : " + ip);
    }
    if (ip == null) {
        ip = request.getHeader("HTTP_CLIENT_IP");
        log.info("HTTP_CLIENT_IP : " + ip);
    }
    if (ip == null) {
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        log.info("HTTP_X_FORWARDED_FOR : " + ip);
    }
    if (ip == null) {
        ip = request.getRemoteAddr();
        log.info("getRemoteAddr : "+ip);
    }
    log.info("Result : IP Address : "+ip);

    return ip;
}

}