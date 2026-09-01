package com.example.firstproject.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.example.firstproject.CustomError.CustomException;

/**
 * 스프링 컨텍스트를 안 띄우는 순수 단위 테스트다.
 * (다른 테스트들과 달리 DB·Redis·시크릿이 없어도 돈다)
 *   실행:  ./mvnw -Dtest=ImageExtensionTest test
 */
class ImageExtensionTest {

	private MockMultipartFile file(String filename) {
		return new MockMultipartFile("image", filename, null, "내용".getBytes());
	}

	@Test
	@DisplayName("허용된 확장자는 소문자로 정규화해서 돌려준다")
	void 허용확장자() {
		assertEquals(".png", ImageExtension.resolve(file("사진.png")));
		assertEquals(".jpg", ImageExtension.resolve(file("사진.JPG")));
		assertEquals(".jpeg", ImageExtension.resolve(file("a.jpeg")));
		assertEquals(".gif", ImageExtension.resolve(file("a.gif")));
		assertEquals(".webp", ImageExtension.resolve(file("a.webp")));
	}

	@Test
	@DisplayName("HTML 을 올리면 막는다 - 저장형 XSS 차단")
	void 에이치티엠엘차단() {
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("evil.html")));
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("evil.htm")));
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("evil.svg")));
	}

	@Test
	@DisplayName("확장자가 없거나 경로가 섞이면 막는다 - 경로 탈출 차단")
	void 경로탈출차단() {
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("확장자없음")));
		// 역슬래시는 이스케이프 없이 문자코드로 만든다(윈도우 경로 흉내)
		String bs = String.valueOf((char) 92);
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("a." + bs + ".." + bs + "evil")));
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("a./../../evil")));
	}

	@Test
	@DisplayName("이중 확장자는 마지막 것으로 판단한다")
	void 이중확장자() {
		// evil.png.html 은 실제로 html 로 서빙되므로 막아야 한다
		assertThrows(CustomException.class, () -> ImageExtension.resolve(file("evil.png.html")));
		// 반대로 evil.html.png 는 png 로 서빙되므로 통과시켜도 안전하다
		assertEquals(".png", ImageExtension.resolve(file("evil.html.png")));
	}
}
