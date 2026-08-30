package com.example.firstproject.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.NoticeDetailDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Dto.NoticeImageDto;
import com.example.firstproject.Dto.NoticeUpdate;
import com.example.firstproject.Dto.Noticeform;
import com.example.firstproject.Dto.TwitformnoticeDto;
import com.example.firstproject.Dto.detachVo;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Dto.Previewimage.PreviewimageDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;

public interface NoticeService {

	//게시판가져오기
	public Page<TwitformnoticeDto> read(Long userid,String option,String keywordm,int page);
	
	public List<NoticeDto> readfd(int page);
	
	public void noticecreate(Noticeform form);
	
	public void delete(Long num);
	
	public NoticeDto noticeupdate(Long num,NoticeUpdate update);
	
	public NoticeDetailDto detail(Long noticeid,Long userid);
	
	public void Commentcreate(Commentform form);
	
	public List<CommentDto> commentget(int num);
	
	public Page<NoticeDto> search(Long loginid,String option,String content,int page);
	
	public void commentupdate(Long id,String email,String text);
	
	public void commentdelete(Long id);
	
	public void sendalarm(String userid,int noticenum,String noticetitle);
	
	public String contentimagesave(MultipartFile image);
	
	public void saveimagecut(String id ,String path);
	
	public void garbagefiles();
	
	public ResponseEntity getdetach(detachVo detach);
	
	public ResponseEntity noticelikes(MemberEntity member,Long noticeid);
	
	public boolean noticelikecheck(MemberEntity member,Long noticeid);
	
	public  Page<TwitformnoticeDto> favoritenotice(MemberEntity member,Pageable pageable,String option,String keyword);

	public Page<TwitformnoticeDto> followingnotice(Long userid, Pageable pageable);
	
	public Page<NoticeDto> loginnoticeget(Long userid,int page);
	
	public Page<NoticeDto> loginnoticesearchget(Long userid,String option,String content,int page);


	public Page<CommentDto> showcomments(Long noticeid,int page);
	
	public Page<NoticeImageDto> getimagelist(Long userid,int page,String option,String keyword);
	
	public List<PreviewimageDto> getPreviewimage(Long userid,Long noticeid);

	public Page<TwitformnoticeDto> userpagenotice(Long loginid,int page,Long userid,String option,String keyword,String sortoption);
	//NoticeDetailDto detail(Long noticeid, Long userid);
	
	public Page<NoticeImageDto> getuserpageimagelist(Long userid,String option,String keyword,int page,Long loginid);

}
