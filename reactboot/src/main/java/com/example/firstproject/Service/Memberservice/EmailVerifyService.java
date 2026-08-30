package com.example.firstproject.Service.Memberservice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.example.firstproject.Entity.EmailVerification;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Repository.EmailVerificationRepository;
import com.example.firstproject.Service.mailservice.mailsandservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 인증 토큰 발급 / 검증.
 *
 * 예전 방식과 달라진 점
 *  - 토큰을 member.auth 컬럼에 넣지 않는다(그 컬럼은 이제 "N"/"Y" 상태만 담는다).
 *  - URL 에 이메일을 싣지 않는다. 토큰 하나로 회원까지 특정된다.
 *  - 만료(24시간)와 1회용이 생겼고, 재발송하면 이전 링크가 무효가 된다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerifyService {

	/** 검증 결과. 화면 문구가 네 가지로 갈린다. */
	public enum Result {
		OK,       //방금 인증 완료
		ALREADY,  //이미 인증된 계정(링크를 두 번 눌렀거나 재발송으로 무효화된 옛 링크)
		EXPIRED,  //기한 지남 → 재발송 안내
		INVALID   //그런 토큰이 없음
	}

	public static final String PURPOSE_SIGNUP = "EMAIL_VERIFY";

	private static final long EXPIREHOURS = 24;
	private static final long RESENDCOOLDOWNSECONDS = 60;
	private static final int RESENDDAILYLIMIT = 5;
	private static final int TOKENBYTES = 32; //256비트. 추측은 사실상 불가능하다.

	private final EmailVerificationRepository repo;
	private final MemberHandler memberhandler;
	private final mailsandservice mailservice;

	private final SecureRandom random = new SecureRandom();

	//──────────────────────────────────────────────────────────
	// 발급
	//──────────────────────────────────────────────────────────
	@Transactional
	public void issue(MemberEntity member) {

		LocalDateTime now = LocalDateTime.now();

		//이 회원의 살아있는 토큰을 전부 닫는다 → 항상 마지막 메일만 유효
		repo.invalidateall(member.getId(), PURPOSE_SIGNUP, now);

		String token = randomtoken();          //원본 — 메일로만 나간다
		repo.save(EmailVerification.builder()
				.member(member)
				.tokenhash(sha256hex(token))   //DB 에는 해시만
				.purpose(PURPOSE_SIGNUP)
				.expiresat(now.plusHours(EXPIREHOURS))
				.createdat(now)
				.build());

		mailservice.sendverifymail(member.getUsername(), token);
		log.info("이메일 인증 토큰 발급 memberid={}", member.getId());
	}

	//──────────────────────────────────────────────────────────
	// 검증
	//──────────────────────────────────────────────────────────
	@Transactional
	public Result verify(String token) {

		if (token == null || token.trim().isEmpty()) {
			return Result.INVALID;
		}

		//받은 원본을 같은 방식으로 다시 해시해서 조회한다(복호화가 아니다).
		EmailVerification row = repo.findByTokenhash(sha256hex(token)).orElse(null);
		if (row == null) {
			return Result.INVALID;
		}
		if (row.isused()) {
			return Result.ALREADY;
		}
		if (row.isexpired(LocalDateTime.now())) {
			return Result.EXPIRED;
		}

		row.setUsedat(LocalDateTime.now());
		repo.save(row);

		MemberEntity member = row.getMember();
		member.setAuth("Y");              //로그인 게이트(PrincipalService)가 보는 값
		memberhandler.membercreate(member);

		log.info("이메일 인증 완료 memberid={}", member.getId());
		return Result.OK;
	}

	//──────────────────────────────────────────────────────────
	// 재발송
	//──────────────────────────────────────────────────────────
	/**
	 * 재발송. 성공/실패를 호출부에 알려주긴 하지만,
	 * <b>사용자에게 보여줄 응답은 항상 같아야 한다.</b>
	 * "그런 계정 없습니다"를 알려주면 이메일 존재 여부가 새어 나간다.
	 */
	@Transactional
	public boolean resend(String email) {

		if (email == null || email.trim().isEmpty()) {
			return false;
		}

		Optional<MemberEntity> found = memberhandler.findemail(email.trim());
		if (found.isEmpty()) {
			return false;
		}
		MemberEntity member = found.get();

		if ("Y".equals(member.getAuth())) {
			return false; //이미 인증된 계정. 메일을 보낼 이유가 없다.
		}

		LocalDateTime now = LocalDateTime.now();

		//쿨다운 — 연타로 메일을 쏟아내지 못하게
		Optional<EmailVerification> last =
				repo.findFirstByMemberIdAndPurposeOrderByCreatedatDesc(member.getId(), PURPOSE_SIGNUP);
		if (last.isPresent() && last.get().getCreatedat().isAfter(now.minusSeconds(RESENDCOOLDOWNSECONDS))) {
			log.info("재발송 쿨다운 memberid={}", member.getId());
			return false;
		}

		//하루 한도 — 메일 발송은 평판(스팸 신고)이 걸린 동작이다
		long today = repo.countByMemberIdAndPurposeAndCreatedatAfter(
				member.getId(), PURPOSE_SIGNUP, now.minusDays(1));
		if (today >= RESENDDAILYLIMIT) {
			log.info("재발송 하루 한도 초과 memberid={}", member.getId());
			return false;
		}

		issue(member);
		return true;
	}

	//──────────────────────────────────────────────────────────
	// 토큰 유틸
	//──────────────────────────────────────────────────────────
	/** URL 에 그대로 실을 수 있는 랜덤 문자열(Base64URL, 43자). */
	private String randomtoken() {
		byte[] bytes = new byte[TOKENBYTES];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	/**
	 * 토큰은 사람이 만든 값이 아니라 256비트 난수라, 무차별 대입이 애초에 불가능하다.
	 * 그래서 비밀번호와 달리 느린 해시(BCrypt)가 필요 없고,
	 * 조회 키로 쓸 수 있는 빠르고 결정적인 SHA-256 이 맞다.
	 */
	private String sha256hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(hashed.length * 2);
			for (byte b : hashed) {
				sb.append(Character.forDigit((b >> 4) & 0xF, 16));
				sb.append(Character.forDigit(b & 0xF, 16));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			//SHA-256 은 모든 JVM 이 반드시 지원한다. 여기 올 일은 없다.
			throw new IllegalStateException(e);
		}
	}
}
