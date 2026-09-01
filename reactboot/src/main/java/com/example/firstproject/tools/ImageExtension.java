package com.example.firstproject.tools;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;

/**
 * 업로드된 파일의 확장자를 검사해서 돌려준다.
 *
 * 왜 필요한가:
 *   업로드 파일은 WebConfig.addResourceHandlers 가 등록한 /userprofileimg/**,
 *   /noticeimages/** 로 그대로 서빙된다. 정적 리소스의 Content-Type 은 확장자로 정해진다.
 *   따라서 확장자를 검사하지 않으면 x.html 을 올려서 API 도메인에서 실행되는
 *   HTML 을 심을 수 있다(저장형 XSS). 쿠키/토큰이 같은 도메인에 있으므로 피해가 크다.
 *
 *   또 원본 파일명에는 경로 구분자가 섞여 올 수 있다("a.\..\..\evil" 등).
 *   화이트리스트를 통과하지 못하면 저장 자체를 안 하므로 그 경로도 같이 막힌다.
 */
public class ImageExtension {

	// 이미지로 서빙해도 안전한 것만 허용한다.
	// svg 는 넣지 않는다 - 안에 <script> 를 담을 수 있어서 이미지인 척하는 XSS 가 된다.
	private static final Set<String> ALLOWED = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");

	/**
	 * 파일명에 확장자가 없을 때 쓰는 대체 판단표.
	 *
	 * 프론트가 캔버스로 리사이즈한 뒤 Blob 으로 올리는 경로가 있는데
	 * (Twitnoticecreate 등 4곳), Blob 에 파일명을 안 주면 브라우저가
	 * filename="blob" 으로 보낸다. 확장자가 없으니 위 화이트리스트만으로는
	 * 정상 업로드가 전부 막힌다.
	 *
	 * Content-Type 도 업로더가 정하는 값이라 그 자체를 신뢰하지는 않는다.
	 * 다만 여기서 고를 수 있는 결과가 이미지 확장자뿐이라, 어떤 값을 보내든
	 * 저장되는 파일은 이미지로만 서빙된다. html 로 서빙시키는 길은 막힌 채다.
	 */
	private static final Map<String, String> BY_CONTENT_TYPE = Map.of(
			"image/png", ".png",
			"image/jpeg", ".jpg",
			"image/jpg", ".jpg",
			"image/gif", ".gif",
			"image/webp", ".webp");

	private ImageExtension() {
	}

	/**
	 * 허용된 확장자면 "." 을 포함한 소문자 확장자를 돌려주고,
	 * 아니면 400 으로 요청을 끊는다(ErrorController 가 받아 준다).
	 */
	public static String resolve(MultipartFile file) {
		String origin = file.getOriginalFilename();
		String ext = "";
		if (origin != null && origin.lastIndexOf('.') > -1) {
			ext = origin.substring(origin.lastIndexOf('.')).toLowerCase();
		}
		if (ALLOWED.contains(ext)) {
			return ext;
		}

		//파일명으로 판단이 안 되면 Content-Type 을 본다(위 BY_CONTENT_TYPE 주석 참고).
		String type = file.getContentType();
		if (type != null) {
			//"image/png; charset=..." 처럼 파라미터가 붙어 오는 경우가 있다.
			String mime = type.split(";")[0].trim().toLowerCase();
			String byType = BY_CONTENT_TYPE.get(mime);
			if (byType != null) {
				return byType;
			}
		}

		throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.NOT_ALLOW_IMAGE_TYPE);
	}
}
