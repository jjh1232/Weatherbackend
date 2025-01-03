package com.example.firstproject.Entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.follow.FollowEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name="member")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="member")
@EntityListeners(AuditingEntityListener.class)//이거 createDate 핅수
public class MemberEntity {

	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	
	@Column(unique = true,nullable = false)
	private String username;
	@Column(nullable = false)
	private String password;
	
	private String profileimg;
	
	@Column(unique=false,nullable=false)
	private String nickname;
	@CreatedDate
	@Column(updatable = false, name="REGDATE") //업데이트불가
	private LocalDateTime regdate;
	
	private String role;
	
	private String refreshtoken;
	
	private String provider;
	private String providerid;
	
	@Embedded //서로관련있는걸 따로클래스로 만들어 임베디드타입으로사용 
	private Address homeaddress; //속성을분리하대 Db테이블은따로만들기싫을때
	
	private String auth;
	
	//팔로우 목록추가
	
	@OneToMany(mappedBy="member",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private List<NoticeEntity> notices;
	
	// @OnDelete(action = OnDeleteAction.SET_NULL) 이함수로 널값으로 게시글으남길수도있다
	//근데이쪽이아니라 연관관계주인인 many쪽에 근데 null로나와도 이상한거같아서 처리고민중
	@OneToMany(mappedBy="member",fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private List<CommentEntity> comments;
	
	//알림
	@OneToMany(mappedBy="member", fetch=FetchType.LAZY,cascade = CascadeType.ALL)
	private List<Notification> notifications;
	
	//채팅방목록 //다대다관계는 실무에서못쓰고 중간테이블을 만들어 일대다로 생성
	//주인필드의 변수명 얘는이거수정안하고조회만
	@OneToMany(mappedBy="member", fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	private List<MemberRoom> chatrooms;
	
	
	public void addnotices(NoticeEntity notice) {
		notices.add(notice);
	}
	public void addcomments(CommentEntity comment) {
		comments.add(comment);
	}
	
	public void addnotifications(Notification notifi) {
		notifications.add(notifi);
	}
	public void addchatroom(MemberRoom room) {
		chatrooms.add(room);
	}
	@LastModifiedDate
	@Column(updatable= true,name="UPDATEREGDATE")
	private LocalDateTime updatered;
	
	 //팔로우================================================
	
    @OneToMany(mappedBy = "frommember", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<FollowEntity> followings;

    @OneToMany(mappedBy = "tomember", fetch = FetchType.LAZY ,cascade = CascadeType.ALL)
    private List<FollowEntity> followers;
	
    //좋아요 기능---============================================
    //일단cascade로 하는데  @noDelete 로 null주려해도메소드가안뜸;
    //아니면 일일히 다 null값으로 set해야하는데 그거까지않닌거같아서
    @OneToMany(mappedBy="member",fetch= FetchType.LAZY,cascade = CascadeType.ALL)
    private List<FavoriteEntity> favorite;
    
	public MemberDto toDto(Long id,
			String username,String password,String nickname,String role,
			String refreshtoken,String provider,String providerid,Address region,
			LocalDateTime red,LocalDateTime updatered) {
		return MemberDto.builder()
				.id(id)
				.username(username)
				.password(password)
				.nickname(nickname)
				.role(role)
				.refreshtoken(refreshtoken)
				.provider(provider)
				.providerid(providerid)
				.homeaddress(region)
				.red(red)
				.updatered(updatered)
				
				.build();
	}

	
}
