package com.example.firstproject.tools;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.firstproject.CustomError.CustomException;
import com.example.firstproject.CustomError.ErrorCode;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Repository.CommentRepository;
import com.example.firstproject.Repository.NoticeRepository;
import com.example.firstproject.Repository.roomrepo.MemberRoomRepository;
import com.example.firstproject.configure.PrincipalDetails;

import lombok.RequiredArgsConstructor;

/**
 * 「이 사람 것이 맞나」 를 확인한다.
 *
 * <p>인가 필터는 「로그인했나」 만 본다. 그래서 글 번호·댓글 번호만 바꿔 보내면
 * 남의 글을 고치거나 지울 수 있었다(IDOR, 2026-09-15 점검).
 * 화면에서 버튼을 숨기는 것은 막는 게 아니다 — 요청은 누구나 직접 만들 수 있다.
 *
 * <p>규칙 두 가지.
 * <ul>
 *   <li>「누가 요청했나」 는 요청 본문이 아니라 로그인 정보에서 꺼낸다.</li>
 *   <li>고치거나 지우기 전에 대상의 주인을 **회원 id** 로 비교한다.
 *       글·댓글에 복사돼 있는 username 문자열은 나중에 어긋날 수 있다.</li>
 * </ul>
 *
 * <p>관리자 기능은 {@code /admin/**} 의 별도 서비스라 여기를 거치지 않는다.
 * open-in-view 가 꺼져 있어 LAZY 연관을 읽으려면 트랜잭션이 필요하다.
 */
@Component
@RequiredArgsConstructor
public class OwnerCheck {

	private final NoticeRepository noticerepo;
	private final CommentRepository commentrepo;
	private final MemberRoomRepository memberroomrepo;

	/** 로그인한 회원 id. 인가 필터를 통과했으면 비어 있을 수 없다. */
	public Long me(Authentication authentication) {
		if (authentication == null || !(authentication.getPrincipal() instanceof PrincipalDetails)) {
			throw new CustomException(HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN);
		}
		return ((PrincipalDetails) authentication.getPrincipal()).getMember().getId();
	}

	@Transactional(readOnly = true)
	public void notice(Long noticeid, Long memberid) {
		NoticeEntity notice = noticerepo.findById(noticeid)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_NOTICE));
		if (notice.getMember() == null || !notice.getMember().getId().equals(memberid)) {
			throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.NOT_OWNER);
		}
	}

	@Transactional(readOnly = true)
	public void comment(Long commentid, Long memberid) {
		CommentEntity comment = commentrepo.findById(commentid)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_COMMENT));
		if (comment.getMember() == null || !comment.getMember().getId().equals(memberid)) {
			throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.NOT_OWNER);
		}
	}

	/** 채팅방 참여자인가. 방 내용 조회·초대·메시지 발행·구독 전에 본다. */
	@Transactional(readOnly = true)
	public boolean isRoomMember(Long roomid, Long memberid) {
		return roomid != null && memberid != null
				&& memberroomrepo.findByRoom_idAndMember_Id(roomid, memberid).isPresent();
	}

	public void roomMember(Long roomid, Long memberid) {
		if (!isRoomMember(roomid, memberid)) {
			throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.NOT_ROOM_MEMBER);
		}
	}

	/** 방 이름은 참여자마다 따로 둔다(MemberRoom). 내 MemberRoom 만 바꿀 수 있다. */
	@Transactional(readOnly = true)
	public void memberRoom(Long memberroomid, Long memberid) {
		MemberRoom mr = memberroomrepo.findById(memberroomid)
				.orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_MEMBERROOM));
		if (mr.getMember() == null || !mr.getMember().getId().equals(memberid)) {
			throw new CustomException(HttpStatus.FORBIDDEN, ErrorCode.NOT_OWNER);
		}
	}
}
