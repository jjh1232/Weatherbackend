package com.example.firstproject.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeUpdate;
import com.example.firstproject.Dto.Noticeform;
import com.example.firstproject.Dto.detachVo;
import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Commentform;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;

public interface NoticeService {

	
	public Page<NoticeDto> read(Pageable page);
	
	public List<NoticeDto> readfd(int page);
	
	public void noticecreate(Noticeform form);
	
	public void delete(Long num);
	
	public NoticeDto noticeupdate(Long num,NoticeUpdate update);
	
	public NoticeDto detail(Long num);
	
	public void Commentcreate(Commentform form);
	
	public List<CommentDto> commentget(int num);
	
	public Page<NoticeDto> search(String option,String content,int page);
	
	public void commentupdate(Long id,String email,String text);
	
	public void commentdelete(Long id);
	
	public void sendalarm(String userid,int noticenum,String noticetitle);
	
	public String contentimagesave(MultipartFile image);
	
	public void saveimagecut(String id ,String path);
	
	public void garbagefiles();
	
	public ResponseEntity getdetach(detachVo detach);
	
	public ResponseEntity noticelikes(MemberEntity member,Long noticeid);
	
	public boolean noticelikecheck(MemberEntity member,Long noticeid);
}
