package com.example.firstproject.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import lombok.RequiredArgsConstructor;

/**
 * 메일 템플릿을 브라우저에서 바로 확인하는 개발용 화면.
 *
 * <p>계정을 지웠다 다시 만들지 않아도 메일이 어떻게 보이는지 볼 수 있다.
 * 실제 발송과 <b>같은 템플릿·같은 변수</b>를 그대로 렌더하므로 눈으로 확인하는 용도로 충분하다.
 * (다만 메일 클라이언트마다 CSS 지원이 달라서, 최종 확인은 실제 발송으로 해야 한다.)
 *
 * <p><b>운영에서는 뜨지 않는다.</b> 배포할 때 <code>SPRING_PROFILES_ACTIVE=prod</code> 를 주면
 * 이 컨트롤러 자체가 등록되지 않는다.
 *
 * <pre>
 *   http://localhost:8081/open/dev/mailpreview/email        가입 인증
 *   http://localhost:8081/open/dev/mailpreview/passfind     임시 비밀번호
 *   http://localhost:8081/open/dev/mailpreview/deletemail   탈퇴 코드
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@Profile("!prod")
public class MailPreviewController {

	private final SpringTemplateEngine templateengine;

	@Value("${app.base-url}")
	private String baseurl;

	@Value("${app.contact-email}")
	private String contactemail;

	@GetMapping(value = "/open/dev/mailpreview/{type}", produces = MediaType.TEXT_HTML_VALUE)
	public String preview(@PathVariable String type,
			@RequestParam(required = false, defaultValue = "someone@example.com") String username) {

		if (!Map.of("email", 1, "passfind", 1, "deletemail", 1).containsKey(type)) {
			return "<h3>알 수 없는 템플릿입니다: " + type + "</h3>"
					+ "<p>email / passfind / deletemail 중 하나를 쓰세요.</p>";
		}

		String samplecode = "Kd7Q2xM9";
		String sampletoken = "Xq7kP2mZ9vB4nT8sJ1wR6yH3uL0aF5cE2dG7iK4oP9s";

		Context context = new Context();
		context.setVariable("username", username);
		context.setVariable("code", samplecode);
		context.setVariable("contactemail", contactemail);
		context.setVariable("baseurl", baseurl);
		context.setVariable("verifyurl",
				baseurl + "/open/member/register?token=" + sampletoken);

		return templateengine.process(type, context);
	}
}
