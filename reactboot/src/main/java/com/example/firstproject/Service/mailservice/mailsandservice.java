package com.example.firstproject.Service.mailservice;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.example.firstproject.Vo.EmailMessage;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor //롬북과연동해 이어노테이션과 final로 주입을받을수있다 (특정변수만만생성하는생성자)
public class mailsandservice {

	//autowired는 주입받을객체가하나면 ㅍ스프링에서알아서 주입해줌 따라서 귀찬게안해도 이러면주입댐
	
	private final JavaMailSender javamailsender;
	
	private final SpringTemplateEngine templateengine;//타임리프라이브러리 jsp연결하는거임 부트는뷰리졸버설정도안하는듯?
	
	private final BCryptPasswordEncoder encode;
	
	//메일 본문에서 쓰는 값들. 배포 환경마다 달라지므로 설정에서 읽는다.
	@Value("${app.base-url}")
	private String baseurl;
	
	@Value("${app.contact-email}")
	private String contactemail;
	
	//메일보내기 타입으로 지정
	public String sendmail(EmailMessage emailmessage,String type) 
	{
		MimeMessage mimeMessage=javamailsender.createMimeMessage();
		
		String newpass=createCode();
		String authkey=encode.encode(newpass);
		try {
			//참고로 뒤에트루펄스는 multipart파일유무
			MimeMessageHelper mimemessagehelper= new MimeMessageHelper(mimeMessage, false, "UTF-8");
			mimemessagehelper.setTo(emailmessage.getTo());//수신자
			mimemessagehelper.setSubject(emailmessage.getSubject());//메일제목
			if(type.equals("email")) {
				
			
			
			mimemessagehelper.setText(setContext(emailmessage.getTo(),authkey, type),true);
			//본문내용,html여부
			}
			if(type.equals("passfind")) {
				mimemessagehelper.setText(setContext(emailmessage.getTo(),newpass, type),true);
			}
			if(type.equals("deletemail")) {
				mimemessagehelper.setText(setContext(emailmessage.getTo(), authkey, type),true);
			}
			
			
			javamailsender.send(mimeMessage);
		
			
			
			return authkey;
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			log.info("error");
			throw new RuntimeException(e);
			
		}
		
	}
	
	 /**
     * 가입 인증 메일. 링크에는 <b>원본 토큰만</b> 실린다(이메일을 싣지 않는다).
     * 토큰 생성과 저장은 EmailVerifyService 가 맡고, 여기서는 보내기만 한다.
     */
    public void sendverifymail(String to, String token) {
        MimeMessage mimeMessage = javamailsender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[Weave] 이메일 인증을 완료해 주세요");

            Context context = new Context();
            context.setVariable("username", to);
            context.setVariable("contactemail", contactemail);
            context.setVariable("verifyurl",
                    baseurl + "/open/member/register?token=" + urlencode(token));

            helper.setText(templateengine.process("email", context), true);
            javamailsender.send(mimeMessage);
        } catch (MessagingException e) {
            log.info("인증메일 발송 실패");
            throw new RuntimeException(e);
        }
    }


    // 인증번호 및 임시 비밀번호 생성 메서드
    public String createCode() {
        Random random = new Random();
        StringBuffer key = new StringBuffer();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(4);

            switch (index) {
                case 0: key.append((char) ((int) random.nextInt(26) + 97)); break;
                case 1: key.append((char) ((int) random.nextInt(26) + 65)); break;
                default: key.append(random.nextInt(9));
            }
        }
        return key.toString();
    }
    
    
 //타임리프라이브러리사용해서 html간편하게보내기 
    private String urlencode(String v) {
        return URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8);
    }

    public String setContext(String username,String code, String type) {
        Context context = new Context();
        //변수선언
        context.setVariable("code", code);
        context.setVariable("username", username);
        //링크 주소와 문의처는 템플릿에 박지 않고 여기서 넣어준다.
        context.setVariable("baseurl", baseurl);
        context.setVariable("contactemail", contactemail);
        //가입 인증 링크는 여기서 통째로 만들어 넘긴다.
        //authkey 는 BCrypt 해시라 '/', '.', '$' 가 섞여 있어서 반드시 인코딩해야 한다.
        context.setVariable("verifyurl", baseurl + "/open/member/register"
                + "?username=" + urlencode(username)
                + "&authokey=" + urlencode(code));
        //타임리프엔진
        //디펜던시설정시 알아서 해당하는html로 감 type에맞게 
        //폴더추가시 그주소까지 넣거나 yml파일에 타임리프설정추가
        return templateengine.process(type, context);//타임리프엔진 (리소스파일명,콘텍스트변수설정)
    }
}
	
	

