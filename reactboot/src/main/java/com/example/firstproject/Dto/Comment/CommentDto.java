package com.example.firstproject.Dto.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.firstproject.Entity.CommentEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class CommentDto {



	private Long id;
	private Long cid;
	private Long noticenum;
	private int depth;
	private Long cnum;
	private String username;
	private String nickname;
	private String text;
	private String redtime;
	private String userprofile;
	private boolean isdelete;
	//운영자 차단 여부. 화면에서 "차단됨" 표시와 차단해제 버튼에 쓴다.
	private boolean isblocked;
	//프로필 링크/표시용 핸들
	private String profileid;
	//자식용추가
	//빌더패턴사용시 이렇게안하면 빌드값을안해도 초기값이 무시됨 생성할떄 new 를 작성하거나 
	//아래어노테이션사용안하면 null이된다
	@Builder.Default
	private List<CommentDto> childs=new ArrayList<>();
	
	
	public CommentEntity toEntity(Long id,int depth,Long cnum,String username,String name,
			String text,boolean isdelete) {
		return CommentEntity.builder()
				.id(id)
				
				.depth(depth)
				.cnum(cnum)
				.username(username)
				.nickname(nickname)
				.text(text)
				.isdelete(isdelete)
				
				.build();
	}
	
	
	//생성자 직접만들어야 dto 프러덕션용 롬북은 모두있는거나 없는것도해서 인식을못하나봄
	public CommentDto(Long id, Long cid, Long noticenum, int depth, Long cnum, String username, String nickname,
			String text, String redtime, String userprofile, boolean isdelete, String profileid) {
		this(id,cid,noticenum,depth,cnum,username,nickname,text,redtime,userprofile,isdelete,false,profileid);
	}

	//JPQL 생성자 표현식용. isblocked 가 늘어나 인자 순서가 헷갈리므로 위 생성자는 이걸 부른다.
	public CommentDto(Long id, Long cid, Long noticenum, int depth, Long cnum, String username, String nickname,
			String text, String redtime, String userprofile, boolean isdelete, boolean isblocked, String profileid) {
		
		this.id = id;
		this.cid = cid;
		this.noticenum = noticenum;
		this.depth = depth;
		this.cnum = cnum;
		this.username = username;
		this.nickname = nickname;
		this.text = text;
		this.redtime = redtime;
		this.userprofile = userprofile;
		this.isdelete = isdelete;
		this.isblocked = isblocked;
		this.profileid = profileid;
	}
	
	
}
