package com.example.firstproject.Dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.firstproject.Entity.CommentEntity;
import com.example.firstproject.Entity.FavoriteEntity;
import com.example.firstproject.Entity.detachfile;

// join 사용시 인터페이스로 받아야함 
public interface NoticeDtointer {
	
 Long getNoticeid();
	
	
	 String getNoticeuser();
	
	 String getnoticenick();
	
	 String getprofileimg();
	
	
	 String gettitle();
		
	 String gettext();
	
	 String gettemp();
	
	 String getsky();
	 String getpty();
	
	
	 String getrain();
	
	//팔로우 목록추가
	 String getred();
	
	 List<detachfile> getfiles();
	

	List<CommentEntity> getcomments();
	
	
	
	
	
	
	
	

    //좋아요 기능---============================================
    //일단cascade로 하는데  @noDelete 로 null주려해도메소드가안뜸;
    //아니면 일일히 다 null값으로 set해야하는데 그거까지않닌거같아서
   
     List<FavoriteEntity> getlikeuser();
}
