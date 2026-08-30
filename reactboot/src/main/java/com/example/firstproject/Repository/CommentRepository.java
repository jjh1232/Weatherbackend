package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.firstproject.Dto.Comment.CommentDto;
import com.example.firstproject.Dto.Comment.Testcom;
import com.example.firstproject.Entity.CommentEntity;


public interface CommentRepository extends JpaRepository<CommentEntity,Long>{

	
	@Query(value="Select * from comment where notice_id =:noticenum",nativeQuery=true)
	List<CommentEntity> findbynoticenum(@Param("noticenum") Long num);
	
	

	Page<CommentEntity> findByUsernameContaining(Pageable page,String username);
	
	Page<CommentEntity> findByNicknameContaining(Pageable page,String nickname);
	
	@Query(value="Select * from comment where notice_id =:noticeid",nativeQuery=true)
	Page<CommentEntity> findByNoticeIdContaining(Pageable page,Long noticeid);
	
	
	
	//long countBycnum(Long commentid);
	//존재여부만확인
	boolean existsByCnum(Long cnum);
	
	//fetch조인은 다가져와서 별로고 직접매핑으로필요한것만 클래스명으로 작성해야함
	//이걸로 부모만 가져오고 아래 자식은 페치조인으로가져오자
		@Query(value = "SELECT new com.example.firstproject.Dto.Comment.CommentDto" +
			       "(c.id,m.id,n.id,"
			       + "c.depth,c.cnum,"
			       + "m.username,m.nickname, "
			       //차단이 삭제보다 앞이다. 둘 다인 경우 운영자 조치를 보여주는 게 맞다.
			       + "CASE WHEN c.isblocked = true THEN '운영자에 의해 차단된 댓글입니다' "
			       + "     WHEN c.isdelete = true THEN '삭제된 댓글입니다' ELSE c.text END, "
			       + "c.createdDate,m.profileimg,c.isdelete,c.isblocked,m.profileid) " +   //연관객체도  
			       "FROM CommentEntity c JOIN c.notice n "
			       + "JOIN c.member m "
			       + "where n.id=:noticeid AND c.depth =0")
		Page<CommentDto> showcomments(Pageable pageable,Long noticeid);//,Long noticeid);

	//생각해보니 이것도 dto프로덕션이 더나은거같긴함
		@Query("SELECT c FROM CommentEntity c " +
			       "JOIN FETCH c.member m " +
			       "JOIN FETCH c.notice n " +
			       "WHERE n.id = :noticeid AND c.cnum IN :parentIds " +
			       //정렬을 안 주면 DB가 내주는 순서에 맡겨진다. 답글은 대화라 오래된순 고정.
			       "ORDER BY c.createdDate ASC")
			List<CommentEntity> findChildComments(Long noticeid, List<Long> parentIds);
	
		/* 이코드는 페이징이랑 fetchjoin같이몼씀 
		//위코드는 부모10개에 자식다가져오기 가 불가능 때문에 fetchjoin활용
		@Query("SELECT c FROM CommentEntity c "
			     + "JOIN FETCH c.member m "
			     + "JOIN FETCH c.notice n "
			     + "WHERE n.id = :noticeid AND c.depth = 0")
				Page<CommentEntity> findParentComments(Pageable pageable,Long noticeid);
		*/
		//자식 가져오기
}