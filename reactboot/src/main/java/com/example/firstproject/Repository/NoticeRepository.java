package com.example.firstproject.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.NoticeDetailDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Dto.NoticeImageDto;
import com.example.firstproject.Dto.noticeDao;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.NoticeEntity;

@Repository

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long>{
	
	//====================================서브쿼리연습용=======================================
	@Query(value="Select * from notice order by red DESC limit :start,:end",nativeQuery = true)
	List<NoticeEntity> findnotice(@Param("start") int start,@Param("end") int end);
	//=====================================진짜연습==================================
	@Query(value="Select n"
								
			
			+ " from notice n left join n.member m "
			+ " left join n.comments c on n.id = c.notice"
			
			
			//+ "left join n.files f "
			//+ "left join comments c where n.comments.id = c.id"
			//+ "left join n.likeuser u "
			//+ "left join n.member m "
			//+ "join fetch n.likeuser u"
			//+ "left join fetch n.detachfiles d on n.id=d.notice_id "
			
			//@BatchSize와 default_batch_fetch_size
			//jpa내에서 알아서 캐싱해서 where문으로 찾는다고함 근데 적절한커넥션풀로설정안하면db에큰부담
					
	
			,nativeQuery = false)
	Page<NoticeEntity> test113(Pageable pageable);
	//List<NoticeEntity> findByredDESC(Pageable page);
	
	
	//=====================================일단 eager이라도써서돌아가게============================================
	//Page<NoticeEntity> findAll(Pageable pageable);
	//jpa는on지원안한다고함
	//@Query(value="select n.* from notice as n join member as m on m.id=n.member_id where n.text like %:text%",nativeQuery = true)
	@Query(value="Select * from notice   where title like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtitle(@Param("text") String text,Pageable pageable);
	
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.text like %:text%")
	@Query(value="Select * from notice   where text like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchtext(@Param("text") String text,Pageable pageable	);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.nickname like %:text%")
	@Query(value="Select * from notice   where noticenick like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchname(@Param("text") String text,Pageable pageable	);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.title like %:text% or text like %:text%")
	@Query(value="Select * from notice where text like %:text% or title like %:text% ",nativeQuery=true)
	Page<NoticeEntity> searchtitletext(@Param("text") String text,Pageable pageable);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.nickname like %:text%")
	@Query(value="Select * from notice  where noticenick like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchnoticeex(@Param("text") String text,Pageable page);
	
	
	@Query(value="select n from notice n where n.member.id=:userid")
	Page<NoticeEntity> findbyidall(Long userid, Pageable page);
	
	Page<NoticeEntity> findByMember(MemberEntity member,Pageable page);
	
	//좋아요한게시글다이렉트 dto프로덕션이 젤좋은데 일단 fetch조인도사용해봄 이거 영속화해서 get시바로가져옴 근데 페이지객체랑쓰려면 
	//카운트쿼리작성해야함 
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member",
			countQuery="select count(n) from notice n join n.likeuser f where f.member=:member" )
	Page<NoticeEntity> getfavoritenotice(MemberEntity member,Pageable page);

	//이미지 만 데려오는 코드 
	/* jpql은 select절에 서브쿼리를못사용해서 불편
	@Query(value="select new com.example.firstproject.Dto.NoticeImageDto("
			+"n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(f) from detachfile f where f.notice =n) "
			+ ") "
			+ "from notice n "
			+ "join n.member m "
			+ "join n.files d "
			+ "where d.id=("
			+ "select min(d2.id) from detachfile d2 where d2.notice = n"
			+ ")"
			)
	Page<NoticeImageDto> findimagelist(Pageable page);
*/
		//카운트는 *든 id든 null이아닌행만가져오기때문에 인덱스만 있으면성능차이x
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id)",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id)",
			nativeQuery = true)
	Page<Object[]> findimagelist(Pageable page);

	@Query(value="select n.id,m.username,m.nickname,n.title,n.text,"
			+ "n.temp,n.sky,n.pty,n.rain,n.reh,n.wsd,n.red,m.username"
			+ "from notice n join n.member m "
			+ "where n.id=:noticeid")
	NoticeDetailDto findbyid(Long noticeid);
}
