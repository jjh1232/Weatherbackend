package com.example.firstproject.Entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 메일로 보내는 1회용 확인 토큰.
 *
 * 핵심: <b>원본 토큰은 여기 저장하지 않는다.</b>
 *  - 사용자에게 가는 링크 : ?token=<원본>          (메일함에만 존재)
 *  - 이 테이블            : sha256(원본)          (DB에만 존재)
 * 검증할 때는 받은 원본을 다시 SHA-256 해서 tokenhash 로 조회한다.
 * SHA-256 은 같은 입력이면 항상 같은 값이라 WHERE 절로 바로 찾을 수 있다
 * (BCrypt 는 salt 때문에 매번 달라서 조회 키로 쓸 수 없다).
 *
 * 이 구조라서 URL 에 이메일을 실을 필요가 없고,
 * DB 가 통째로 유출돼도 해시로는 링크를 만들지 못한다.
 */
@Entity
@Table(
    name = "email_verification",
    indexes = {
        @Index(name = "idx_ev_tokenhash", columnList = "tokenhash"),
        @Index(name = "idx_ev_member", columnList = "member_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;

	/** sha256(원본토큰) 을 hex 로. 64자 고정. */
	@Column(nullable = false, unique = true, length = 64)
	private String tokenhash;

	/** EMAIL_VERIFY / PASSWORD_RESET — 나중에 비밀번호 재설정도 이 테이블을 쓴다. */
	@Column(nullable = false, length = 20)
	private String purpose;

	@Column(nullable = false)
	private LocalDateTime expiresat;

	/**
	 * 사용(또는 무효화)된 시각.
	 * 행을 지우지 않고 이 값을 남기는 이유: 지워버리면 "없는 토큰"과 "이미 쓴 토큰"을
	 * 구분할 수 없어서, 링크를 두 번 누른 사람에게 "잘못된 링크"라고 하게 된다.
	 */
	private LocalDateTime usedat;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdat;

	@PrePersist
	public void onpersist() {
		if (this.createdat == null) {
			this.createdat = LocalDateTime.now();
		}
	}

	public boolean isused() {
		return this.usedat != null;
	}

	public boolean isexpired(LocalDateTime now) {
		return this.expiresat.isBefore(now);
	}
}
