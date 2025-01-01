package com.example.firstproject.Service.mailservice;

import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    public String setContext(String username,String code, String type) {
        Context context = new Context();
        context.setVariable("code", code);
        context.setVariable("username", username);
        
        return templateengine.process(type, context);//타임리프엔진 (리소스파일명,콘텍스트변수설정)
    }
}
	
	

