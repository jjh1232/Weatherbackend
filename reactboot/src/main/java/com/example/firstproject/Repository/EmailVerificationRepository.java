package com.example.firstproject.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.EmailVerification;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

	/** 검증의 핵심. 해시 하나로 "어떤 회원의 어떤 요청인지"까지 특정된다. */
	Optional<EmailVerification> findByTokenhash(String tokenhash);

	/**
	 * 새로 발급하기 전에 이 회원의 살아있는 토큰을 전부 닫는다.
	 * 이렇게 해야 "가장 최근에 보낸 메일의 링크만" 유효해진다.
	 * 옛 메일을 누른 사람에게는 만료 안내가 나간다.
	 */
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("update EmailVerification e set e.usedat = :now "
			+ "where e.member.id = :memberid and e.purpose = :purpose and e.usedat is null")
	int invalidateall(@Param("memberid") Long memberid,
			@Param("purpose") String purpose,
			@Param("now") LocalDateTime now);

	/** 재발송 쿨다운 계산용 — 가장 최근 발급 건. */
	Optional<EmailVerification> findFirstByMemberIdAndPurposeOrderByCreatedatDesc(Long memberid, String purpose);

	/** 하루 발송 한도 계산용. */
	long countByMemberIdAndPurposeAndCreatedatAfter(Long memberid, String purpose, LocalDateTime after);

	/** 오래된 찌꺼기 정리용(스케줄러에서 호출). */
	@Modifying
	@Query("delete from EmailVerification e where e.expiresat < :before")
	int deleteexpiredbefore(@Param("before") LocalDateTime before);
}
