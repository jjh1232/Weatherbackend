package com.example.firstproject.tools;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpStatus;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;

/**
 * 업로드 폴더 안의 파일 경로를 안전하게 만든다.
 *
 * <p>왜 필요한가:
 * 예전에는 클라이언트가 준 값을 그대로 이어붙였다.
 * <pre>
 *   Paths.get(uploadroot + path)     // path = "/../../../etc/hostname"
 *   → /opt/weathertw/uploads/../../../etc/hostname
 *   → /etc/hostname
 * </pre>
 * {@code /open/atagdown} 은 인증 없이 열려 있어서, 누구나 서버의 아무 파일이나
 * 내려받을 수 있었다. 마운트된 {@code /app/application-secret.yml} 까지 읽히면
 * JWT 서명키가 넘어가고, 그 키로 관리자 토큰을 위조할 수 있다.
 *
 * <p>여기서는 정규화한 뒤 업로드 폴더 안에 있는지 확인하고, 벗어나면 거부한다.
 * "../" 를 문자열로 걸러내는 방식은 인코딩(%2e%2e%2f)이나 표기 차이로 새기 때문에,
 * 실제 경로를 계산해서 뿌리와 비교하는 이 방식이 맞다.
 */
public final class UploadPath {

	private UploadPath() {
	}

	/**
	 * 업로드 뿌리 아래의 실제 경로를 돌려준다.
	 * 뿌리를 벗어나면 400 으로 끊는다.
	 *
	 * @param uploadroot app.upload.public-dir 값
	 * @param path       "/noticeimages/2026/09/01/uuid.png" 같은 상대 경로.
	 *                   저장 값이 절대주소이거나 뒤에 ?ban=3 이 붙어 있어도 받아준다.
	 */
	public static Path resolve(String uploadroot, String path) {
		if (path == null || path.isBlank()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_UPLOAD_PATH);
		}

		String rel = path.trim();

		//저장된 값이 "https://api.도메인/noticeimages/..." 처럼 절대주소인 경우가 있다.
		//호스트 부분을 떼고 경로만 남긴다.
		int scheme = rel.indexOf("://");
		if (scheme > -1) {
			String afterhost = rel.substring(scheme + 3);
			int slash = afterhost.indexOf('/');
			rel = (slash > -1) ? afterhost.substring(slash) : "/";
		}

		//차단 이미지 경로에 붙는 ?ban=3 같은 쿼리는 파일명이 아니다.
		int query = rel.indexOf('?');
		if (query > -1) {
			rel = rel.substring(0, query);
		}

		//resolve 는 인자가 "/" 로 시작하면 뿌리를 무시하고 절대경로가 된다.
		while (rel.startsWith("/")) {
			rel = rel.substring(1);
		}
		if (rel.isEmpty()) {
			throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_UPLOAD_PATH);
		}

		Path root = Paths.get(uploadroot).toAbsolutePath().normalize();
		Path target = root.resolve(rel).normalize();

		//정규화까지 마친 뒤에 비교해야 "a/../../b" 같은 것도 걸러진다.
		if (!target.startsWith(root)) {
			throw new CustomException(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_UPLOAD_PATH);
		}

		return target;
	}
}
