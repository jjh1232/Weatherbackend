package com.example.firstproject.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.example.firstproject.Dto.Detachupdateform;
import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Dto.NoticeUpdate;
import com.example.firstproject.Dto.Noticeform;
import com.example.firstproject.Dto.datachfiledto;
import com.example.firstproject.Dto.detachVo;
import com.example.firstproject.Dto.removetestDto;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.detachfile;
import com.example.firstproject.Entity.follow.FollowEntity;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Handler.NoticeHandler;
import com.example.firstproject.Repository.DetachfileRepository;
import com.example.firstproject.Service.Memberservice.SseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class NoticeServiceImpl implements NoticeService {

	@Autowired
	private NoticeHandler noticehandler;
	
	@Autowired
	private SimpMessageSendingOperations operations;

	private final MemberHandler memberhandler;
	
	private final DetachfileRepository detachrepo;
	
	private final SseService sseservice;
	
	@Override
	public Page<NoticeDto> read(Pageable page) {
		System.out.println("게시판리드서비스");
		Page<NoticeEntity> entity = noticehandler.read(page);
		
		/*
		Page<NoticeDto> dtlist =entity.map
				(m->m.toDto(m.getNoticeid(),
						m.getNoticeuser(),m.getNoticenick(), m.getTitle()
						,m.getText(),m.getRed()
						,m.getLikeuser().size()
						,m.getTemp(),m.getSky(),m.getPty(),m.getRain()
						,m.getMember().getProfileimg()
						)
						
						);
		*/
		Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
		//Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto());//페이지맵핑dto로
		
		// TODO Auto-generated method stub
		return dtlist;
	}

	@Override
	public Page<NoticeDto> search(String option, String content,int page) {
		// TODO Auto-generated method stub
		log.info("서비스시작"+option);
		PageRequest pageable =PageRequest.of(page-1, 10,Sort.by(Sort.DEFAULT_DIRECTION.DESC,"red"));
		//페이지객체옵션
		
		
		if (option.equals("titletext")) {
			log.info("타이틀+텍스트서비스");
			String option1="title";
			String option2="text";
			Page<NoticeEntity> entity=noticehandler.searchtitletext(content,pageable);
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
		
		
			return dtlist;
		}
		else if(option.equals("title")){
			log.info("타이틀서비스");
		
			
			Page<NoticeEntity> entity=noticehandler.searchtitle(content,pageable);
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
			return dtlist;
		}
		else if(option.equals("text")) {
			log.info("텍스트서비스");
			Page<NoticeEntity> entity=noticehandler.searchtext(content,pageable);
			
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
			List<NoticeDto> ddd=dtlist.getContent();
			log.info(ddd.toString());
			return dtlist;
		}
		else {
			log.info("네임서비스");
			Page<NoticeEntity> entity=noticehandler.searchname(content,pageable);
			Page<NoticeDto> dtlist=entity.map(m-> new NoticeDto(m));
			return dtlist;
		}
			
		
		
	}

	  
	 @Override 
	 public List<NoticeDto> readfd(int page) { // TODO
	   List<NoticeEntity> entity =noticehandler.readfd(page);
	   List<NoticeDto> dtlist=new ArrayList();
	  for(NoticeEntity a:entity) { NoticeDto dto =a.toDto(a.getNoticeid(),a.getNoticeuser(),
	  a.getNoticenick(),a.getTitle(),a.getText(),a.getRed(),a.getLikeuser().size()
	  ,a.getTemp(),a.getSky(),a.getPty(),a.getRain()
			  );
	  dtlist.add(dto); }
	  
	 return dtlist; 
	 }



	@Override
	public void noticecreate(Noticeform form) {
		// TODO Auto-generated method stub
		MemberEntity member=memberhandler.findemail(form.getUsername()).orElseThrow();
		System.out.println("멤버찾고 들어온폼내용"+form.toString());
	
		
			
	 NoticeEntity Entity =NoticeEntity.builder()
			 			.noticeuser(form.getUsername())
			 			.noticenick(form.getNickname())
			 			.title(form.getTitle())
			 			.text(form.getText())
			 			.member(member)
			 			.temp(form.getTemp())
			 			.sky(form.getSky())
			 			.pty(form.getPty())
			 			.rain(form.getRain())
			 			.build();
	 log.info(form.getFiles().toString());
	 if(form.getFiles() != null) {
		 log.info("이미지파일존재함!");
		 
	 
		List<detachfile> detachfiles=new ArrayList<>();
	 for(datachfiledto file:form.getFiles()) {
			if(file.getUrl().equals("")) {
				log.info("널이왜있어씹;");
			}else {
				log.info(form.toString());
			detachfile detachentity=detachfile.builder()
					.filename(file.getFilename())
					.idx(file.getIdx())
					.path(file.getUrl())
					.rangeindex(file.getIndex())
					.notice(Entity)
					.member(member)
					.build();
			log.info("디태치엔티티:"+detachentity);
			Entity.addfiles(detachentity);
			}
			}
	 }
	 
	 		System.out.println("게시판엔티티생성후");
	 		member.addnotices(Entity);	
	 		log.info("파일즈:"+form.getFiles());
			
			
			
	 		System.out.println("멤버에 리스트에추가");
			 noticehandler.create(Entity);
			 System.out.println("엔티티생성");
			 
			
		
	}



	@Override
	public void delete(Long num) {
		// TODO Auto-generated method stub
		noticehandler.delete(num);
	}


	//게시글업데이트
	@Override
	public NoticeDto noticeupdate(Long num,NoticeUpdate update) {
		// TODO Auto-generated method stub
		log.info("업데이트서비스시작");
		Optional<NoticeEntity> find=noticehandler.findbyId(num);
		NoticeEntity Entity=find.get();
		Entity.setTitle(update.getTitle());
		Entity.setText(update.getText());
		
		log.info(Entity.getFiles().toString());
		List<removetestDto> remove=new ArrayList<>();
		List<detachfile> newdetach=new ArrayList<>();
		
		Iterator<detachfile> dbfileiterator=Entity.getFiles().iterator();
	
		
					
		
		while(dbfileiterator.hasNext()) {
				detachfile dbdata=dbfileiterator.next(); //다음값삽입
				
				removetestDto removedto=removetestDto.builder().id(dbdata.getId()).url(dbdata.getPath()).test(false).build();
				remove.add(removedto);
		}
		for(removetestDto removedata:remove) {
			log.info("삭제체크기존데이터:"+removedata.getId());
			for(Detachupdateform data:update.getDetach()) {
					log.info("폼시작데이터:"+data.getId());
				
					
					if(removedata.getId()==data.getId()) {
						log.info("수정하지않은데이터:"+data.getId());
						removedata.setTest(true);
						break;
						
					}else if(data.getId()==0) {
						log.info("새데이터");
						detachfile detach=detachfile.builder()
								.idx(data.getIdx())
								.rangeindex(data.getRangeindex())
								.filename(data.getFilename())
								.path(data.getPath())
								.notice(Entity)
								
								.build();
						
					newdetach.add(detach);
					log.info("이게문제?"+detach.getPath());
					
						break;
					
					}else {
						log.info("해당하지않는데이터");
						
					}
					
									
				
			//폴문끝
			//Entity.setFiles(newdetach);
		}
			
			}
			
		//MemberEntity noticemember=memberhandler.findemail(Entity.getUsername()).get();
		
		//noticehandler.update(Entity);
		
		log.info("삭제예정");
		String filepublic="D:/study프로그램/react/bootproject/public";
		for(removetestDto removes:remove) {
			log.info(removes.getId().toString());
			System.out.println(removes.isTest());
			if(!removes.isTest()) {
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
		Entity.setFiles(newdetach);
		
		NoticeDto dto =Entity.toDto(Entity.getNoticeid(),Entity.getNoticeuser(),
				  Entity.getNoticenick(),Entity.getTitle(),Entity.getText(),Entity.getRed()
				,Entity.getLikeuser().size(),
				Entity.getTemp(),Entity.getSky(),Entity.getPty(),Entity.getRain()
				);;
				  
				  return dto;
	}


	//게시판디테일===================================================
	@Override
	public NoticeDto detail(Long num) {
		
		NoticeEntity Entity=noticehandler.detail(num);
		
		System.out.println("코멘트:"+Entity.getComments());
		NoticeDto dto=Entity.toDto
						(Entity.getNoticeid(),
						Entity.getNoticeuser(),
						Entity.getNoticenick(),
						Entity.getTitle(), 
						Entity.getText(),
						Entity.getRed(),
						Entity.getComments(),
						Entity.getFiles(), 
						Entity.getLikeuser().size(),
						Entity.getTemp(),Entity.getSky(),Entity.getPty(),Entity.getRain()
								);
		
		
		
		// TODO Auto-generated method stub
		return dto;
	}



	@Override
	public void Commentcreate(Commentform form) {
		/*
		CommentEntity Entity = new CommentEntity(null,
				form.getNoticenum(),
				form.getDepth(),
				form.getCnum(),
				form.getUsername(),
				form.getNickname(),
				form.getText(),
				null);
		*/
		Long noticeid=form.getNoticeid();
		System.out.println(noticeid);
		//일단대충
		NoticeEntity notice=noticehandler.findbyId(noticeid).orElseThrow();
		
		MemberEntity member=memberhandler.findemail(form.getUsername()).orElseThrow();
		CommentEntity Entity =CommentEntity.builder()
				
				.depth(form.getDepth())
				.cnum(form.getCnum())
				.username(form.getUsername())
				.nickname(form.getNickname())
				.text(form.getText())
				.notice(notice)
				.member(member)
				.build();
		notice.addcomments(Entity);
		member.addcomments(Entity);
		
		
		
		
		System.out.println("문제여");
		noticehandler.commentcreate(Entity);
		
		if(form.getDepth()==1) {
			System.out.println("대댓글입니다!");
			CommentEntity replyEntity = noticehandler.findcomment((long) form.getCnum()).get();
			//댓글아이디,작성한유저아이디,댓글자체의아이디,작성한노티스제목
			System.out.println("원댓글유저아이디:"+replyEntity.getUsername());
			sseservice.sendtocomment(replyEntity.getMember(),member.getId(),noticeid,notice.getTitle());
		}
		
		else {
		System.out.println("댓글작성!!이후 sse발송");
		//글작성자아이디,보내는사람아이디,글아이디
			
		sseservice.sendtonotice(notice.getMember(),member.getId(),noticeid,notice.getTitle());
		
		}
		
	}



	@Override
	public List<CommentDto> commentget(int nums) {
		Long num=Long.valueOf(nums);
		List<CommentEntity> findlist = noticehandler.commentget(num);
		if(findlist==null||findlist.isEmpty()) {
			
			log.info("값이없음");
			return null;
		}
		else {
			List<CommentDto> dtolist = new ArrayList();
			for(CommentEntity a:findlist) {CommentDto dto = a.toDto(a.getId(),
					a.getDepth(),
					a.getCnum(),
					a.getUsername(),
					a.getNickname(),
					a.getText(),
					a.getCreatedDate(),
					a.getMember().getProfileimg()
					);
			dtolist.add(dto);
			}
			return dtolist;
			
		}
		
		
	}



	


	@Override
	public void commentupdate(Long id, String email, String text) {
		// TODO Auto-generated method stub
			Optional<CommentEntity> opti=noticehandler.findcomment(id);
			if(opti.isEmpty()) {
				log.info("없는코멘트아이디");
			}
			else {
				CommentEntity commententity=opti.get();
				commententity.setText(text);
				noticehandler.commentcreate(commententity);
				
				
			}
	}



	@Override
	public void commentdelete(Long id) {
		// TODO Auto-generated method 
		noticehandler.deletecomment(id);
		
	}



	@Override
	public void sendalarm(String userid,int noticenum,String noticetitle) {
		// TODO Auto-generated method stub
		System.out.println("알람보내기서비스");
		operations.convertAndSend("/sub/"+userid,noticenum+"번 게시글 \""+noticetitle+"\"에 새로운댓글이 있습니다!");
	}


	//이미지세이브 근데리액트외부가안되서 ..
	@Override
	public String contentimagesave(MultipartFile image) {
		// TODO Auto-generated method stub
		//우선폴더생성 
		String filesaveData=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		log.info("날짜포맷생성"+filesaveData);
		String fileforder=filesaveData.replace("/", File.separator);
		log.info("날짜포맷경로sepa:"+fileforder);
		File savefolder=new File("D:/프로젝트간단정리/weathertw/frontend/bootproject/public/noticeimages",fileforder);
		if(savefolder.exists()==false) {//폴더가 있으면 트루없으면폴스
			savefolder.mkdirs();
			
		}
		String uuid=UUID.randomUUID().toString();
		String oriname=image.getOriginalFilename(); //png붙어서그런가
		String savefilename=savefolder.toPath()+File.separator+uuid+"_"+oriname;//+".png";
		log.info("궁금해서topath내용:"+savefolder.toPath());
		Path savePath=Paths.get(savefilename);
		log.info("최종생성경로:"+savePath);
		try {
			image.transferTo(savePath);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch bloc
			log.info("경로오류");
			e.printStackTrace();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("경로오류");
			e.printStackTrace();
			
		}
				
		return filesaveData+"/"+uuid+"_"+oriname;
	}


	//저장이미지삭제 근데 구조상 안만들어도됬을뜨 스케줄러로걍자동삭제 아니면 수정때만사용하던가
	@Override
	public void saveimagecut(String id,String path) {
		// TODO Auto-generated method stub
		log.info("서비스단"+path);
		String pathre="D:/프로젝트간단정리/weathertw/frontend/bootproject/public"+path;
		String repath=pathre.replace("/", File.separator);
		log.info("수정패스:"+repath);
		Path deletepath=Paths.get(repath);
		Long longid=Long.parseLong(id);
		//detachfile detachEntity=detachrepo.findById(longid).orElseThrow();
		try {
			
			Files.delete(deletepath);
			
			//detachrepo.delete(detachEntity);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("서버문제");
		}
	}



	@Override
	@Scheduled(fixedDelay = 1000*60*60*12)//12시간마다삭제
	public void garbagefiles() {
		// TODO Auto-generated method stub
		log.info("경로의 파일 가져오기");
		List<File> filelist= new ArrayList<>();
		String checkdate=LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		
		String urlname="/noticeimages/"+checkdate;
		log.info("현재시간-"+urlname);
		String filedirectory="D:/study프로그램/react/bootproject/public"+urlname;
		String path=filedirectory.replace("/",File.separator);
		log.info("패스:"+path);
		File dir=new File(path);
		
		String[] filenames=dir.list(); //배열로받아야함
			//여기즘파일네임널오류인데 일단보류
		List<removetestDto> removet=new ArrayList<>();
		for(String filename: filenames) {
			removetestDto dto=removetestDto.builder().url(filedirectory+"/"+filename).test(false).build();
			log.info(dto.getUrl());
			removet.add(dto);
			
		}
		
		//db에서 업로드이미지주소가져오기
		
		List<detachfile> dbdata=noticehandler.getdatachfiles(checkdate);
		/* 스트림은 안의내용이바뀐다
		for(detachfile data: dbdata) {
			removet.stream().forEach(file->{
				log.info("로컬폴더:"+file.getUrl());
				log.info("db:"+data.getPath());
				if(file.getUrl().contains(data.getPath())) 
				{
					file.setTest(true);
					
				}
			}
			);둘다셋하면잘바뀜..
							*/
		for(detachfile data:dbdata) {
			for(removetestDto remove:removet) {
				if(remove.getUrl().contains(data.getPath())) {
					remove.setTest(true);
					log.info("트루");
				}
				else {
					
				}
			}
		}
			//파일삭제 
			for(removetestDto file:removet) {
				if(!file.isTest()) {
					log.info("db에없는애들:"+file.getUrl());
					Path remove=Paths.get(file.getUrl());
					try {
						Files.delete(remove);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						log.info("오류");
					}
				}
				
			}
			
		}



	@Override
	public ResponseEntity getdetach(detachVo detach) {
		// TODO Auto-generated method stub
		Path filepath = Paths.get("D:/프로젝트간단정리/weathertw/frontend/bootproject/public"+detach.getUri());
		try {
			UrlResource resource=new UrlResource(filepath.toUri());
			//한글파일이름 꺠질수있으니인코딩
			String encodeupload=UriUtils.encode(detach.getFilename(),StandardCharsets.UTF_8);
			String contentDisposition = "attachment; filename=\"" + encodeupload + "\"";
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION,contentDisposition)
					.body(resource);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.info("오류!");
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Transactional
	public ResponseEntity noticelikes(MemberEntity member, Long noticeid) {
		// TODO Auto-generated method stub
		log.info("좋아요서비스단시작");
		NoticeEntity notice=noticehandler.findbyId(noticeid).orElseThrow(()->new IllegalAccessError("no notice"));
		Optional<FavoriteEntity> found=noticehandler.findbynoticeanduser(member, notice);
		if(found.isEmpty()) {
			//좋아요누른적없음
			FavoriteEntity likes=FavoriteEntity.builder()
					.member(member)
					.notice(notice)
					.build();
			noticehandler.favoritesave(likes);
			//int likesnum=notice.getLikeuser().size()+1;
			return ResponseEntity.ok("좋아요");
		}else {
			//좋아요누른적있음
			noticehandler.favoritedelete(found.get());
			//int likesnum=notice.getLikeuser().size()-1;
			return ResponseEntity.ok("좋아요해제");
		}
		
	}

	@Override
	public boolean noticelikecheck(MemberEntity member, Long noticeid) {
		// TODO Auto-generated method stub
		NoticeEntity notice=noticehandler.findbyId(noticeid).orElseThrow(()->new IllegalAccessError("no notice"));
		boolean like=!noticehandler.findbynoticeanduser(member, notice).isEmpty();
		return like;
		
	}

	//========================좋아요한 글만가져오기=================================
	@Override
	public  Map<String,Object> favoritenotice(MemberEntity member, Pageable pageable) {
		// TODO Auto-generated method stub
		 	Page<FavoriteEntity> followlist=noticehandler.favoritenoticefind(member, pageable);
		 	System.out.println("좋아요글서비스단어떻게적용되나보자");
		 	
		 	//페이지객체생성법
		 	List<NoticeDto> pagedto=new ArrayList<>();
		 	//이거페이징어차피안되서안씀..생각해보니총페이지도필요없음
		 	//Page<NoticeDto> dtolist=new PageImpl<>(pagedto);
		 	
		 	
		 	for(FavoriteEntity favoriteentity:followlist) {
		 		//아이거내가거꾸로좋아요했음
		 		NoticeEntity notice=favoriteentity.getNotice();
		 		NoticeDto dto=NoticeDto.builder().comments(notice.getComments()).detachfiles(notice.getFiles())
		 				.likes(notice.getLikeuser().size()).nickname(notice.getNoticenick()).num(notice.getNoticeid())
		 				.pty(notice.getPty()).temp(notice.getTemp()).sky(notice.getSky()).rain(notice.getRain())
		 				.text(notice.getText()).title(notice.getTitle()).username(notice.getNoticeuser())
		 				.likeusercheck(true)
		 				.red(notice.getRed())
		 				.userprofile(notice.getMember().getProfileimg())
		 				.build();
		 		
		 		pagedto.add(dto);
		 	}
		 	
		 	
		 	Map<String,Object> data=new HashMap<>();
		 	data.put("totalpage", followlist.getTotalPages());
		 	data.put("notice", pagedto);
		return data;
	}
	}
	 
	


