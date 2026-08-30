package com.example.firstproject.admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.Admindetachchangeform;
import com.example.firstproject.Dto.Detachupdateform;
import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.removetestDto;
import com.example.firstproject.Dto.ChatDto.AdminroomdetailDto;
import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.ChatRoomDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.MeseageDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Dto.userdataDto.LoginHistoryDto;
import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.LoginHistory;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Repository.DetachfileRepository;
import com.example.firstproject.admin.form.Admemberupdateform;
import com.example.firstproject.admin.form.Adminmembercreateform;
import com.example.firstproject.admin.form.AdminnoticeUpdateform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class adminService {
	
	
	private final adminhandler adminhandler;
	
	
	private final DetachfileRepository detachrepo;

	//업로드 루트(application.yml: app.upload.public-dir)
	//예전에는 옛 프로젝트의 절대경로가 그대로 박혀 있었다.
	//그 경로는 지금 머신에 없어서, 이 값을 타는 기능은 전부 깨진 상태였다.
	@Value("${app.upload.public-dir}")
	private String uploadroot;
	private final BCryptPasswordEncoder passen;
	//==================================//멤버=========================================
	
	@Transactional(readOnly = true)
	public Page<MemberDto> allmemberget(int page) {
	
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
		Page<MemberEntity> memberentity=adminhandler.memberlistget(pageable);
	
		Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	
				
			
		

		
		return memberlist;
		
	}
	
	//==============멤버 검색============================
	@Transactional(readOnly = true)
	public Page<MemberDto> searchmembers(String option,String keyword,int page){
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"regdate" ));
		
		if(option.equals("email")){
			
			Page<MemberEntity> memberentity=adminhandler.allusernamesearch(pageable,keyword);
			Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	

			return memberlist;
		}
		else if(option.equals("nickname")) {
			Page<MemberEntity> memberentity=adminhandler.allnicknamesearch(pageable,keyword);
		
			Page<MemberDto> memberlist=memberentity.map((m)->
			MemberDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.role(m.getRole())
			.provider(m.getProvider())
			.red(m.getRegdate())
			.homeaddress(m.getHomeaddress())
			.usernotice(m.getNotices().size())
			.usercomments(m.getComments().size())
			.userchatroom(m.getChatrooms().size())
			.build());	

			return memberlist;
		}
		
		return null;
	}
	
	//멤버생성=======================================================
	public String membercreate(Adminmembercreateform form) {
		
		String newpass=passen.encode(form.getPassword());//시큐리티로그인도 인코딩해줌
		Address regions=new Address();
	
		if(form.getRegion().equals("")) {
			System.out.println("레기온빈값");
			regions=Address.builder().juso("서울특별시  종로구  청운효자동").gridx("60").gridy("127").build();
		}
		else {
			System.out.println("레기온있음");
		 regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
		.build();
		}
		System.out.println("크레잇서비스주소확인:"+form.getRegion());
		MemberEntity entity=MemberEntity.builder()
				
				.username(form.getUsername())
				
				.nickname(form.getNickname())
				.password(newpass)
				
				.provider(form.getProvider())
				.providerid(null)
				.homeaddress(regions)
				.role(form.getRole())
				
				.build();
		
		MemberEntity okentity=adminhandler.membercreate(entity);
		
		return okentity.getNickname();
	}
	
	//멤버정보 업데이트 
	@Transactional
	public String memberupdate(Long userid,Admemberupdateform form) throws IllegalAccessException {
	
		Address regions=Address.builder().juso(form.getRegion()).gridx(form.getGridx()).gridy(form.getGridy())
				.build();
	
		MemberEntity okentity=adminhandler.findmember(userid).orElseThrow(()->new IllegalAccessException("해당하는유저없음"));
	
		okentity.setUsername(form.getUsername());
		
		okentity.setNickname(form.getNickname());
		okentity.setProvider(form.getProvider());
		okentity.setHomeaddress(regions);
		okentity.setRole(form.getRole());
		
		return "성공적으로변경";
	}
	
	
	//=================================게시판페이지관리========================================
	@Transactional(readOnly = true)
	public Page<NoticeDto> allnoticeget(int page) {
		
		System.out.println("핸들러시작");
		/* Sort 는 DB 컬럼명이 아니라 JPA "프로퍼티명"으로 찾는다.
		   NoticeEntity 는 @Column(name="id") private Long noticeid 라서
		   "id" 로 주면 No property 'id' found for type 'NoticeEntity' 로 500 이 났다. */
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"noticeid" ));
		Page<NoticeEntity> entity=adminhandler.noticeallget(pageable);
	
		Page<NoticeDto> list=entity.map((m)->
				NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
				.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
				.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
				.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
				.userprofile(m.getMember().getProfileimg())
				.commentcount(m.getComments().size())
				.declaircount(m.getDecles().size())
				.build()
				);
			
		

		
		return list;
		
	}
	
	//===============================================게시글검색==================================
	@Transactional(readOnly = true)
	public Page<NoticeDto> searchnotice(int page,String option,String keyword) throws IllegalAccessException{
		//위 allnoticeget 과 같은 이유로 "id" 가 아니라 "noticeid"
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"noticeid"));
		//케이스문변수중복이안됨;;
		if (option.equals("titletext")) {
			
			String option1="title";
			String option2="text";
			Page<NoticeEntity> entity=adminhandler.searchtitletext(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
			.commentcount(m.getComments().size())
			.userprofile(m.getMember().getProfileimg())
			
			.build()
			);
		
			return list;
			
		}
		else if(option.equals("title")){
			
					
			Page<NoticeEntity> entity=adminhandler.searchtitle(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
			.commentcount(m.getComments().size())
			.userprofile(m.getMember().getProfileimg()).build()
			);
			return list;
		}
		else if(option.equals("text")) {
			
			Page<NoticeEntity> entity=adminhandler.searchtext(keyword,pageable);
			
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
			.commentcount(m.getComments().size())
			.userprofile(m.getMember().getProfileimg()).build()
			);
			return list;
		
		}
		else if(option.equals("email")){
			MemberEntity member=adminhandler.usernamefind(keyword).orElseThrow(()->
			new IllegalAccessException("해당하는유저가없습니다"));
			Page<NoticeEntity> entity=adminhandler.searchusername(member, pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
			.commentcount(m.getComments().size())
			.userprofile(m.getMember().getProfileimg()).build()
			);
			return list;
		}	
		else {
			
			Page<NoticeEntity> entity=adminhandler.searchname(keyword,pageable);
			Page<NoticeDto> list=entity.map((m)->
			NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
			.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
			.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
			.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
			.commentcount(m.getComments().size())
			.userprofile(m.getMember().getProfileimg()).build()
			);
			
			return list;
			
		}

	
		
	}
	
	@Transactional(readOnly = true)
	public NoticeDto noticedetail(Long noticeid) throws IllegalAccessException {
		NoticeEntity m=adminhandler.findbynotice(noticeid).orElseThrow(()->new IllegalAccessException("해당게시글없습니다"));
		System.out.println("유저프로파일이미지"+m.getMember().getProfileimg());
		List<CommentEntity> comment=m.getComments();//댓글정렬
		//생각해보면구지정렬을?
		//comment.sort(Comparator.comparing(CommentEntity::getCreatedDate).reversed());
		List<CommentDto> comdto=new ArrayList<>();
		
			for(CommentEntity a:comment) {CommentDto dto = a.toDto(a.getId(),
					a.getDepth(),
					a.getCnum(),
					a.getUsername(),
					a.getNickname(),
					a.getText(),
					a.getCreatedDate(),
					a.getMember().getProfileimg()
					);
			comdto.add(dto);
		}
			
		NoticeDto dto=	NoticeDto.builder().num(m.getNoticeid()).username(m.getNoticeuser())
				.nickname(m.getNoticenick()).title(m.getTitle()).text(m.getText())
				.likes(m.getLikeuser().size()).temp(m.getTemp()).sky(m.getSky())
				.pty(m.getPty()).rain(m.getRain()).red(m.getRed()).detachfiles(new ArrayList<>(m.getFiles()))
				.comments(comdto)
				.userprofile(m.getMember().getProfileimg()).build();
		
		return dto;
		
	}
	//==============게시글삭제
	public void deletenotice(Long noticeid) throws IllegalAccessException {
		NoticeEntity entity= adminhandler.findbynotice(noticeid).orElseThrow(()->new IllegalAccessException("게시글이없습니다"));
		
		adminhandler.deletenotice(entity);
		
	}
	
	
	//=================================게시판댓글관리========================================
	//=======================================================================================
	/* open-in-view: false 라 컨트롤러까지 영속성 컨텍스트가 따라오지 않는다.
	   아래 map 안에서 m.getMember() / m.getNotice() 같은 LAZY 연관을 건드리므로
	   트랜잭션이 없으면 LazyInitializationException 으로 500 이 난다.
	   (게시글 쪽 allnoticeget 에는 원래 붙어 있었는데 여기만 빠져 있었다) */
	@Transactional(readOnly = true)
	public Page<CommentDto> allCommentrget(int page) {
		
		
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		Page<CommentEntity> entity=adminhandler.commentget(pageable);
	
		Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.isdelete(m.isIsdelete()).isblocked(m.isIsblocked())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			
		

		
		return list;
		
	}
	//차단 안내 이미지. 여기저기 문자열로 박아두면 바꿀 때 빠뜨린다.
	public static final String BANIMAGE="/front/Subimages/chdan.png";

	/*=====================================================================
	  이미지 한 장을 차단 이미지로 바꾼다.

	  (1) detachfiles.path 만 바꾸면 안 된다.
	      글 본문(notice.text)에는 에디터가 넣은
	      <img src="/noticeimages/xxx.png"> 가 HTML 로 그대로 박혀 있어서,
	      path 만 바꿔봐야 첨부목록에서만 차단으로 보이고 글에서는 원본이 보인다.

	  (2) 차단 주소에 ?ban={detachid} 를 붙여 이미지마다 다르게 만든다.
	      한 글에서 두 장을 차단하면 본문에 같은 차단 주소가 두 번 들어가는데,
	      그 상태로 한 장만 복구하면 replace 가 나머지 것까지 원본으로
	      되돌려버린다. 주소가 서로 다르면 그 한 장만 정확히 짚어낼 수 있다.
	      정적 파일 서빙은 쿼리스트링을 무시하므로 보이는 그림은 똑같다.

	  (3) 원본 경로를 originalpath 에 남긴다. 안 남기면 오차단을 못 되돌린다.
	 =====================================================================*/
	private String bannedurl(Long detachid) {
		return BANIMAGE+"?ban="+detachid;
	}
	private boolean isbanned(String path) {
		return path!=null && path.startsWith(BANIMAGE);
	}

	private void banimage(detachfile detach) {
		String oldpath=detach.getPath();

		//이미 차단된 이미지면 할 일이 없다(본문 치환도 하면 안 된다)
		if(isbanned(oldpath)) return;
		if(oldpath==null || oldpath.isEmpty()) return;

		//복구용 원본 보관. 이미 들고 있으면 덮어쓰지 않는다.
		if(detach.getOriginalpath()==null) {
			detach.setOriginalpath(oldpath);
		}

		String banned=bannedurl(detach.getId());
		detach.setPath(banned);

		//글이 지워졌거나 본문이 비었으면 바꿀 게 없다
		NoticeEntity notice=detach.getNotice();
		if(notice==null || notice.getText()==null) return;

		//같은 이미지가 본문에 여러 번 들어가 있을 수 있어 전부 바꾼다.
		//정규식이 아니라 리터럴 치환이어야 한다(경로에 . 이 들어간다).
		notice.setText(notice.getText().replace(oldpath, banned));
	}

	//차단 해제 - 원본으로 되돌린다
	private void restoreimage(detachfile detach) throws IllegalAccessException {
		String originalpath=detach.getOriginalpath();

		/* originalpath 가 없는 건 이 기능이 생기기 전에 차단된 행이다.
		   그때는 원본 주소를 아무 데도 안 남겼으므로 되돌릴 수 없다. */
		if(originalpath==null || originalpath.isEmpty()) {
			throw new IllegalAccessException("원본 정보가 없어 복구할 수 없는 이미지입니다");
		}
		if(!isbanned(detach.getPath())) return;   //차단 상태가 아니면 할 일 없음

		String banned=detach.getPath();
		detach.setPath(originalpath);
		detach.setOriginalpath(null);             //다음에 다시 차단하면 그때 새로 담는다

		NoticeEntity notice=detach.getNotice();
		if(notice==null || notice.getText()==null) return;

		notice.setText(notice.getText().replace(banned, originalpath));
	}

	//부적절한 이미지 변경
	@Transactional
	public Long changeimage(Long detachid) throws IllegalAccessException {

			detachfile detach=adminhandler.detachget(detachid).orElseThrow(()->new IllegalAccessException("해당하는파일없음"));
			banimage(detach);

			return detach.getId();
	}
	//단체 부적절한이미지 변경
	@Transactional
	public void manychangeimage(Admindetachchangeform form) throws IllegalAccessException {
		for (Long detachid:form.getDetachids()) {
			detachfile detach=adminhandler.detachget(detachid).orElseThrow(()->new IllegalAccessException("해당하는파일없음"));
			banimage(detach);
		}
	}
	//차단 해제
	@Transactional
	public Long restoreimage(Long detachid) throws IllegalAccessException {
		detachfile detach=adminhandler.detachget(detachid).orElseThrow(()->new IllegalAccessException("해당하는파일없음"));
		restoreimage(detach);
		return detach.getId();
	}
	//=====================================================================================
	/* [삭제됨] 댓글업데이트
	   운영자가 남의 댓글을 고치는 기능은 두지 않는다(컨트롤러 쪽 주석 참고).
	   참고로 이 메서드는 c.username/c.nickname 을 고쳤는데, 사용자 화면은
	   c.member 조인의 값을 읽는다. 즉 이름을 바꿔도 관리자 화면에서만
	   바뀌고 실제 서비스에는 반영되지 않는 반쪽짜리였다. */
	/* 관리자 삭제.

	   ★ 예전엔 자식 확인 없이 그냥 commentrepo.delete 했다.
	   답글은 cnum(부모 댓글 id)으로 부모를 가리키는데 이건 FK 관계가 아니라
	   그냥 Long 이다. 그래서 답글 달린 원댓글을 지우면 답글들이 DB 에는 남고,
	   화면에서는 depth!=0 이라 원글로도 안 그려지고 부모도 없어서 어디에도
	   안 나오는 유령이 됐다.
	   사용자 삭제(NoticeServiceImpl.commentdelete)는 이미 자식이 있으면
	   isdelete 만 세우고 있었다. 관리자도 같은 규칙을 따른다. */
	@Transactional
	public void commentdelete(Long commentid) throws IllegalAccessException {
		CommentEntity entity=adminhandler.commentfind(commentid).orElseThrow(()->new IllegalAccessException("없는댓글이빈다"));

		if(adminhandler.hascomentchild(commentid)) {
			entity.setIsdelete(true);   //스레드를 남긴다
		}else {
			adminhandler.deletecomment(entity);
		}
	}

	/*=====================================================================
	  운영자 댓글 차단 / 해제.
	  삭제와 달리 원문을 지우지 않는다. 보여줄 때만 안내 문구로 바뀌므로
	  오차단이면 그대로 되돌릴 수 있고, 관리자 화면에서는 원문이 계속 보인다.
	 =====================================================================*/
	@Transactional
	public Long commentblock(Long commentid) throws IllegalAccessException {
		CommentEntity entity=adminhandler.commentfind(commentid).orElseThrow(()->new IllegalAccessException("없는댓글입니다"));
		entity.setIsblocked(true);
		return entity.getId();
	}

	@Transactional
	public Long commentunblock(Long commentid) throws IllegalAccessException {
		CommentEntity entity=adminhandler.commentfind(commentid).orElseThrow(()->new IllegalAccessException("없는댓글입니다"));
		entity.setIsblocked(false);
		return entity.getId();
	}
	//게시글검색조건===========================================================================
	//allCommentrget 과 같은 이유(LAZY 연관 접근)로 트랜잭션이 필요하다
	@Transactional(readOnly = true)
	public Page<CommentDto> commentsearch(int page,String option,String keyword){
		
	
	
		if(option.equals("email")) {
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
			Page<CommentEntity> entity=adminhandler.emailcomment(pageable, keyword);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.isdelete(m.isIsdelete()).isblocked(m.isIsblocked())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
					
			return list;
		}else if(option.equals("nickname")) {
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
			Page<CommentEntity> entity=adminhandler.nicknamecomment(pageable, keyword);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.isdelete(m.isIsdelete()).isblocked(m.isIsblocked())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			return list;
		}else if (option.equals("noticenum")) {
			//리포지토리에서 노티스객체를 못받아서 네이티브쿼리로처리했는데 그럴시 page객체에 sql문 원문그대로놓아줘야하는듯?
			//어쩔수없이 if문마다 다르게 pageable작성
			Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"created_date" ));
			
			System.out.println("게시글번호:"+keyword);
			Long noticeid=Long.parseLong(keyword);
			Page<CommentEntity> entity=adminhandler.noticenumcomment(pageable, noticeid);
			Page<CommentDto> list=entity.map((m)->
			CommentDto.builder().id(m.getId())
			.username(m.getUsername())
			.nickname(m.getNickname())
			.text(m.getText()).depth(m.getDepth()).cnum(m.getCnum())
			.redtime(m.getCreatedDate()).userprofile(m.getMember().getProfileimg())
			.isdelete(m.isIsdelete()).isblocked(m.isIsblocked())
			.noticenum(m.getNotice().getNoticeid())
					
			.build());	
				
			return list;
		}
		else {
			System.out.println("잘못된검색옵션입니다");
			return null;
		}
			}
	
	//게시글업데이트 어드민 버전
	@Transactional
	public void noticeupdate(Long noticeid,AdminnoticeUpdateform form) throws IllegalAccessException {
		NoticeEntity notice=adminhandler.findbynotice(noticeid).orElseThrow(()->new IllegalAccessException("해당게시글없음"));
		notice.setNoticeuser(form.getUsername());
		notice.setNoticenick(form.getNickname());
		notice.setTitle(form.getTitle());
		notice.setText(form.getText());
		notice.setTemp(form.getTemp());
		notice.setSky(form.getSky());
		notice.setPty(form.getPty());
		notice.setRain(form.getRain());
		
		MemberEntity member=notice.getMember();
	
			log.info("파일데이터"+notice.getFiles());
			List<removetestDto> remove=new ArrayList<>();
			List<detachfile> newdetach=new ArrayList<>();
		if(notice.getFiles().isEmpty()) {
					System.out.println("기존값비었음");
					if(!form.getFiles().isEmpty()) {
						System.out.println("새로운이미지있음");
						for(Detachupdateform data:form.getFiles()) {
						detachfile detach=detachfile.builder()
								.idx(data.getIdx())
								.rangeindex(data.getRangeindex())
								.filename(data.getFilename())
								.path(data.getPath())
								.notice(notice)
								.member(member)
								.build();
						
						member.adddetachfiles(detach);
						newdetach.add(detach);
						
						}
						notice.setFiles(newdetach);
						
						
					}
		}
		else {
				//이미지파일처리
				System.out.println("기존값 안비었음");
				Iterator<detachfile> dbfileiterator=notice.getFiles().iterator();
		while(dbfileiterator.hasNext()) {
				detachfile dbdata=dbfileiterator.next(); //다음값삽입
				
				removetestDto removedto=removetestDto.builder().id(dbdata.getId()).idx(dbdata.getIdx()).url(dbdata.getPath()).test(false).build();
				remove.add(removedto);
				
		}
		
			
		
		
			for(Detachupdateform data:form.getFiles()) {
					log.info("폼시작데이터:"+data.getId());
				
				for(removetestDto removedata:remove) {
						log.info("삭제체크기존데이터:"+removedata.getId());
					if(removedata.getIdx()==data.getIdx()) {
						log.info("수정하지않은데이터:"+data.getId());
						removedata.setTest(true);
						data.setCurrent(true);
						break ;
						
					}
					
										
					
				}
				
				if(!data.isCurrent()) {
				log.info("해당하지않는데이터"+data.getId());
				log.info("새데이터");
				detachfile detach=detachfile.builder()
						.idx(data.getIdx())
						.rangeindex(data.getRangeindex())
						.filename(data.getFilename())
						.path(data.getPath())
						.notice(notice)
						.member(member)
						.build();
				
			newdetach.add(detach);
				
			log.info("이게문제?"+detach.getPath());
				}
		
			
			}
			
		//MemberEntity noticemember=memberhandler.findemail(Entity.getUsername()).get();
		
		//noticehandler.update(Entity);
		
	
		String filepublic=uploadroot;
		for(removetestDto removes:remove) {
			log.info(removes.getId().toString());
			System.out.println(removes.isTest());
			if(!removes.isTest()) {
				log.info("삭제예정"+removes.getId().toString());
				detachrepo.deleteById(removes.getId());//db에서삭제
				//삭제되는지확인
				String deletepath=filepublic+removes.getUrl();
				String asd=deletepath.replace("/",File.separator);//파일삭제패스
				Path removepath=Paths.get(asd);
				try {
					Files.delete(removepath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		notice.setFiles(newdetach);
				}
		
	}
	
	
	
	
	@Transactional(readOnly = true)
	public NoticeDto getnoticedetail(Long noticeid) throws IllegalAccessException {
NoticeEntity Entity=adminhandler.noticedetail(noticeid);
		
		System.out.println("코멘트:"+Entity.getComments());
		NoticeDto dto=Entity.toDto
						(Entity.getNoticeid(),
						Entity.getNoticeuser(),
						Entity.getNoticenick(),
						Entity.getTitle(), 
						Entity.getText(),
						Entity.getRed(),
						new ArrayList<>(Entity.getComments()),
						new ArrayList<>(Entity.getFiles()), 
						Entity.getLikeuser().size(),
						Entity.getTemp(),Entity.getSky(),Entity.getPty(),Entity.getRain(),Entity.getReh(),Entity.getWsd(),Entity.getViews()
								);
		
		
		
		// TODO Auto-generated method stub
		return dto;
	}
	
	//============================채티방관련 서비스================================================
	//기본채팅방
	@Transactional(readOnly = true)
	public Page<roomlistresponseDto> allRoomget(int page) {
		
		System.out.println("핸들러시작");
		Pageable pageable=PageRequest.of(page-1, 20,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		Page<Room> entity=adminhandler.chatroomallget(pageable);
	
		Page<roomlistresponseDto> list=entity.map((m)->
		roomlistresponseDto.builder().roomid(m.getId()).roomname(m.getRoomname())
		.namelist(new HashSet<>(m.getUserlist())).red(m.getCreatedDate())
		.chatnum(m.getChatdata().size())
		.latelychat(m.getChatdata().get(m.getChatdata().size()-1).getSender()+":"+m.getChatdata().get(m.getChatdata().size()-1).getMessage())
		.lastchatred(m.getChatdata().get(m.getChatdata().size()-1).getCreatedDate())
		.build());
				
			
		

		
		return list;
		
	}
	
	//채팅방삭제
	@Transactional
	public Long roomdelete(Long roomid) throws IllegalAccessException {
		Room roomentity=adminhandler.roomget(roomid).orElseThrow(()->new IllegalAccessException("룸없음"));
		adminhandler.deleteroom(roomentity);
		return roomentity.getId();
	}
	//채팅방디테일 수정필요 매세지구조가바뀜==================================================
	@Transactional(readOnly = true)
	public Roomdata roomdetail(Long roomid) throws IllegalAccessException {
		Room room=adminhandler.roomget(roomid).orElseThrow(()->new IllegalAccessException("룸없음"));
		
		List<EzmemberDto> memberlist=room.getUserlist().stream()
				.map(ul-> EzmemberDto.builder()
						.userid(ul.getMember().getId())
						.email(ul.getMember().getUsername())
						.nickname(ul.getMember().getNickname())
						.profileurl(ul.getMember().getProfileimg())
						.build()
						
						
						).collect(Collectors.toList());
		
		List<MeseageDto> chatdatas=room.getChatdata().stream().map(
				c->MeseageDto.builder().chatid(c.getId())
				.roomid(roomid)
				.messagetype(c.getMessageType())
				.message(c.getMessage())
				.red(c.getCreatedDate())
				.sender(EzmemberDto.builder()
						.userid(c.getMember().getId())
						.email(c.getMember().getUsername())
						.nickname(c.getMember().getNickname())
						.profileurl(c.getMember().getProfileimg())
						.build())
				.build()
				)
				.collect(Collectors.toList());
		
		return Roomdata.builder().roomid(room.getId())
				.roomname(room.getRoomname())
				.createred(room.getCreatedDate())
				.memberlist(memberlist)
				.chatdata(chatdatas)
				.build();
		
		
		
	}
	//채팅검색 시
	@Transactional(readOnly = true)
	public Page<roomlistresponseDto> searchrooms(int page,String option,String keyword) throws IllegalAccessException{
		Pageable pageable=PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"createdDate" ));
		if(option.equals("roomname")) {
			Page<Room> entity=adminhandler.roomnamefind(pageable, keyword);
			Page<roomlistresponseDto> list=entity.map((m)->
			roomlistresponseDto.builder().roomid(m.getId()).roomname(m.getRoomname())
			.namelist(new HashSet<>(m.getUserlist())).red(m.getCreatedDate())
			.chatnum(m.getChatdata().size())
			.latelychat(m.getChatdata().get(m.getChatdata().size()-1).getMessage())
			.lastchatred(m.getChatdata().get(m.getChatdata().size()-1).getCreatedDate())
			.build());
			
			return list;
			
		}
		else if(option.equals("partilist")) {
			//MemberEntity member=adminhandler.usernamefind(keyword).orElseThrow(()->new IllegalAccessException("해당하는회원이없습니다"));
			
			Page<MemberRoom> entity=adminhandler.roomnamelistfind(pageable, keyword);
			Page<roomlistresponseDto> list=entity.map((m)->
			roomlistresponseDto.builder().roomid(m.getRoom().getId())
			.roomname(m.getRoom().getRoomname())
			.namelist(new HashSet<>(m.getRoom().getUserlist())).red(m.getRoom().getCreatedDate())
			.chatnum(m.getRoom().getChatdata().size())
			.latelychat(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getMessage())
			.lastchatred(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getCreatedDate())
			.build());
			return list;
		}
		else if (option.equals("email")) {
			//유저네임검색 일단콘테이닝말고 정확한명
			MemberEntity member=adminhandler.usernamefind(keyword).orElseThrow(()->new IllegalAccessException("해당하는회원이없습니다"));
			Page<MemberRoom> entity=adminhandler.roomusernametfind(pageable, member);
			Page<roomlistresponseDto> list=entity.map((m)->
			roomlistresponseDto.builder().roomid(m.getRoom().getId())
			.roomname(m.getRoom().getRoomname())
			.namelist(new HashSet<>(m.getRoom().getUserlist())).red(m.getRoom().getCreatedDate())
			.chatnum(m.getRoom().getChatdata().size())
			.latelychat(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getMessage())
			.lastchatred(m.getRoom().getChatdata().get(m.getRoom().getChatdata().size()-1).getCreatedDate())
			.build());
			return list;
		}
		/*
		else if(option.equals("chattext")) {
			adminhandler.roomchatfind(pageable, keyword);
		}
		*/
		else {
			System.out.println("올바르지않은검색입니다");
			return null;
		}
	}
	
	
	//==================================멤버삭제 서비스=================================
	
	
	
	public String memberdelete(Long userid) throws IllegalAccessException {
		MemberEntity member=adminhandler.findmember(userid).orElseThrow(()->
			 new IllegalAccessException("회원이없습니다")
		);
		
		adminhandler.memberdelete(member);
		
		return "멤버삭제성공";
	}
	
//=================================로그인기록 겟=====================================
	public Page<LoginHistoryDto> finduserhistory(String username,int page,String year,String month,boolean isasc){
		
		Sort.Direction direction=isasc?Sort.Direction.ASC:Sort.Direction.DESC;
		PageRequest pageable=PageRequest.of(page-1, 15,Sort.by(direction,"Logindt"));
		long totalelements=0;
		List<LoginHistory> content=new ArrayList<>();
		//페이지객체초기화 선언 하는버이라고함
		Page<LoginHistory> entity= new PageImpl<>(content,pageable,totalelements);
		if(year.equals("novalue")) {
			System.out.println("년도선택안함");
			entity=adminhandler.findbyloginhistory(username, pageable);
		}else if(month.equals("novalue")) {
			
			System.out.println("달선택안함");
			entity=adminhandler.loginhistorysearch(username,year, pageable);
		}else {
			System.out.println("두가지모두선택할경우");
			entity=adminhandler.loginhistorysearch(username,year+"-"+month, pageable);
		}
		
		//List<LoginHistory> entity=adminhandler.findbyloginhistory(username);
		
		Page<LoginHistoryDto> dtolist=entity.map((m)->
			LoginHistoryDto.builder().username(m.getUserid()).userlocale(m.getUserdata())
			.userip(m.getClientip()).logintime(m.getLogindt()).islogin(m.isIslogin())
			.build());
				
				
		
	
		
		
		return dtolist;
	}
}
