package com.example.firstproject.Dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.example.firstproject.Entity.Address;
import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.Notification;
import com.example.firstproject.Entity.detachfile;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.follow.FollowEntity;

import lombok.Data;

@Data
public class noticeDao {

	//데이터받을거
	private Long noticeid;
	
	
	private String noticeuser;
	
	private String noticenick;
	
	private String profileimg;
	
	
	private String title;
		
	private String text;
	
	private String temp;
	
	private String sky;
	private String pty;
	
	
	private String rain;
	
	//팔로우 목록추가
	
	
	private List<detachfile> files;
	

	private List<CommentEntity> comments;
	
	
	
	
	
	
	private LocalDateTime red;
	

    //좋아요 기능---============================================
    //일단cascade로 하는데  @noDelete 로 null주려해도메소드가안뜸;
    //아니면 일일히 다 null값으로 set해야하는데 그거까지않닌거같아서
   
    private List<FavoriteEntity> likeuser;
}
