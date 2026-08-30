package com.example.firstproject.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.MemberEntity;

import net.minidev.json.JSONObject;

/**
 * 화면 표시용 유저 정보(userinfo)를 프론트로 넘긴다.
 *
 * <p><b>쿠키가 아니라 응답 헤더로 보낸다.</b> 예전엔 다섯 군데에서 각자
 * {@code new Cookie("userinfo", ...)} 를 굽고 있었다. 로컬에서는 8081 과 3001 이 둘 다
 * {@code localhost} 라(쿠키는 포트를 구분하지 않는다) 프론트가 읽을 수 있었지만,
 * 배포해서 {@code api.도메인} 과 프론트 도메인으로 갈라지면 그 쿠키는 API 도메인 소유가 되어
 * 프론트의 {@code document.cookie} 에서 아예 보이지 않는다.
 * 로그인은 성공하는데 앱은 누가 로그인했는지 모르는 상태가 된다.
 *
 * <p>토큰과 같은 방식(응답 헤더)으로 통일한다. CORS 의 {@code exposedHeaders} 에
 * {@code userinfo} 가 이미 들어 있어 프론트가 읽을 수 있다.
 * 프론트는 이 헤더를 받으면 자기 도메인 쿠키에 저장한다(CreateAxios 응답 인터셉터).
 *
 * <p>헤더 값에는 ASCII 만 넣을 수 있어 URL 인코딩한다(닉네임·주소에 한글이 들어간다).
 * 예전 쿠키도 같은 방식이라 프론트가 읽는 방법은 바뀌지 않는다.
 *
 * <p>내용은 화면 표시용이다. 권한 판단의 근거는 JWT 이지 이 값이 아니다.
 */
public final class Userinfoheader {

	public static final String HEADER = "userinfo";

	private Userinfoheader() {}

	/** 한 곳에서만 만든다. 예전엔 복사본마다 필드가 조금씩 달랐다
	 *  (memberupdate 응답에는 userid 가 빠져 있어서 프로필 수정 후 채팅·팔로우 목록이 깨졌다). */
	public static JSONObject of(MemberEntity member) {
		JSONObject json = new JSONObject();
		json.put("userid", member.getId());
		json.put("username", member.getUsername());
		json.put("nickname", member.getNickname());
		json.put("profileimg", member.getProfileimg());
		json.put("userrole", member.getRole());
		json.put("profileid", member.getProfileid());

		//주소는 가입 단계에 따라 비어 있을 수 있다. 예전엔 여기서 NPE 가 났다.
		Address home = member.getHomeaddress();
		json.put("region", home == null ? null : home.getJuso());
		json.put("gridx", home == null ? null : home.getGridx());
		json.put("gridy", home == null ? null : home.getGridy());

		return json;
	}

	public static void write(HttpServletResponse response, MemberEntity member) {
		write(response, of(member));
	}

	public static void write(HttpServletResponse response, JSONObject json) {
		try {
			response.setHeader(HEADER, URLEncoder.encode(json.toJSONString(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			//UTF-8 은 모든 JVM 이 지원한다. 여기 올 일은 없지만 화면 정보 하나 때문에
			//로그인 자체를 실패시킬 이유는 없다.
			throw new IllegalStateException(e);
		}
	}
}
