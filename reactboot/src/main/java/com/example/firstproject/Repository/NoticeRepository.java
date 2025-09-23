package com.example.firstproject.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.NoticeDetailDto;
import com.example.firstproject.Dto.NoticeDto;
import com.example.firstproject.Dto.NoticeDtointer;
import com.example.firstproject.Dto.NoticeImageDto;
import com.example.firstproject.Dto.TwitformnoticeDto;
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
	
	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m",
		       countQuery = "select count(n) from notice n join n.member m") //이거패키지이름인데 notice는 Entity네임을 notice로함
	Page<TwitformnoticeDto> twitnoticelist(Long userid,Pageable pageable);
	
	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m " //콘캣이안전하대
		       + "where n.title like concat('%',:text,'%')",
		       countQuery = "select count(n) from notice n join n.member m where n.title like concat('%',:text,'%')")
	Page<TwitformnoticeDto> searchtitle(@Param("text") String text,Long userid,Pageable pageable	);
	
	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m " //콘캣이안전하대
		       + "where n.text like concat('%',:text,'%')",
		       countQuery = "select count(n) from notice n join n.member m where n.text like concat('%',:text,'%')")
	Page<TwitformnoticeDto> searchtext(@Param("text") String text,Long userid,Pageable pageable	);
	
	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m " //콘캣이안전하대
		       + "where n.noticenick like concat('%',:text,'%')",
		       countQuery = "select count(n) from notice n join n.member m where n.noticenick like concat('%',:text,'%')")
	Page<TwitformnoticeDto> searchname(@Param("text") String text,Long userid,Pageable pageable	);
	
	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m " //콘캣이안전하대
		       + "where n.title like concat('%',:text,'%') or n.text like concat('%',:text,'%')",
		       countQuery = "select count(n) from notice n join n.member m where n.title like concat('%',:text,'%') or n.text like concat('%',:text,'%')")
	Page<TwitformnoticeDto> searchtitletext(@Param("text") String text,Long userid,Pageable pageable);
	
	//@Query(value="select n.* from notice n join fetch n.member m where m.id=n.member_id where n.nickname like %:text%")
	@Query(value="Select * from notice  where noticenick like %:text%",nativeQuery=true)
	Page<NoticeEntity> searchnoticeex(@Param("text") String text,Pageable page);
	
	
	@Query(value="select n from notice n where n.member.id=:userid")
	Page<NoticeEntity> findbyidall(Long userid, Pageable page);
	
	Page<NoticeEntity> findByMember(MemberEntity member,Pageable page);
	
	//좋아요한게시글다이렉트 dto프로덕션이 젤좋은데 일단 fetch조인도사용해봄 이거 영속화해서 get시바로가져옴 근데 페이지객체랑쓰려면 
	//카운트쿼리작성해야함 페치조인사용시 오류가많아서
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member",
			countQuery="select count(n) from notice n join n.likeuser f where f.member=:member" )
	Page<NoticeEntity> getfavoritenotice(MemberEntity member,Pageable page);
	//검색메소드
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member "
			+ "and (n.title like %:keyword%)",
			countQuery="select count(n) from notice n join n.likeuser f where f.member = :member and (n.title like %:keyword%)" )	
	Page<NoticeEntity> searchtitlefavoritenotice(MemberEntity member,Pageable page,@Param("keyword") String keyword);
	
	
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member "
			+ "and (n.text like %:keyword%)",
			countQuery="select count(n) from notice n join n.likeuser f where f.member=:member and (n.text like %:keyword%)" )
	Page<NoticeEntity> searchtextfavoritenotice(MemberEntity member,Pageable page,@Param("keyword") String keyword);
	
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member "
			+ "and (n.title like %:keyword% or n.text like %:keyword%)",
			countQuery="select count(n) from notice n join n.likeuser f where f.member=:member and (n.title like %:keyword% or n.text like %:keyword%)" )
	Page<NoticeEntity> searchtitletextfavoritenotice(MemberEntity member,Pageable page,@Param("keyword") String keyword);
	
	@Query(value="select n from notice n JOIN FETCH n.member join n.likeuser f where f.member=:member "
			+ "and (n.noticenick like %:keyword%)",
			countQuery="select count(n) from notice n join n.likeuser f where f.member=:member and (n.noticenick like %:keyword%)" )
	Page<NoticeEntity> searchnicknamefavoritenotice(MemberEntity member,Pageable page,@Param("keyword") String keyword);
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
	
	@Query(value="select new com.example.firstproject.Dto.NoticeDetailDto(n.id,m.username,m.nickname,n.title,n.text,"
			+ "n.temp,n.sky,n.pty,n.rain,n.reh,n.wsd,n.red,m.profileimg,n.views,"
			+ "(select count(1) from FavoriteEntity f where f.notice =n)) "
			+ "from notice n join n.member m "
			+ "where n.id=:noticeid")
	NoticeDetailDto findbyid(Long noticeid);
	
		//카운트는 *든 id든 null이아닌행만가져오기때문에 인덱스만 있으면성능차이x
	//이거 카운트쿼리가 똑같이하면 성능이안좋아서 간단하게 작성했음 join이딱히필요없어서
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id)",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id)",
			nativeQuery = true)
	Page<Object[]> findimagelist(Pageable page);
	
	//제목검색
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
			+ "and n.title like %:keyword%",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
					+ "and n.title like %:keyword%",
			nativeQuery = true)
	Page<Object[]> findtitleimagelist(Pageable page,String keyword);
	
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
			+ "and n.text like %:keyword%",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
					+ "and n.text like %:keyword%",
			nativeQuery = true)
	Page<Object[]> findtextimagelist(Pageable page,String keyword);
	
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
			+ "and n.noticenick like %:keyword%",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
					+ "and n.noticenick like %:keyword%",
			nativeQuery = true)
	Page<Object[]> findnicknameimagelist(Pageable page,String keyword);
	
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
			+ "and n.title like %:keyword% or n.text like %:keyword%",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
					+ "and n.title like %:keyword% or n.text like %:keyword%",
			nativeQuery = true)
	Page<Object[]> findtitletextimagelist(Pageable page,String keyword);
	
	//조회수증가 로직
	//기본적으로 select쿼리만 작동되기때문에 modifying으로 update나insertdelete인걸인지시켜야함
	//보통 제공되는 delete같은걸써서 몰랐음
	@Modifying
	@Query("Update notice n SET n.views =n.views+:views where n.noticeid=:noticeid")
	void updateviewcount(Long noticeid,Long views);

	@Query(value="select new com.example.firstproject.Dto.TwitformnoticeDto(" +
		       "n.noticeid, " +
		       "n.title, " +
		       "m.username, " +
		       "m.nickname, " +
		       "m.profileimg, " +
		       "n.red, "+
		       "n.text,n.pty,n.rain,n.sky,n.temp,n.reh,n.wsd, " + //패이보릿카운트갯수
		       "(select count(f) from FavoriteEntity f where f.notice.noticeid = n.noticeid), " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from FavoriteEntity f2 where f2.notice.noticeid = n.noticeid and f2.member.id = :userid" +
		       ") then true else false end, " +
		       "case when :userid is not null and exists (" +
		       "    select 1 from NoticeblockEntity b where b.noticeid = n.noticeid and b.member.id = :userid" +
		       ") then true else false end, " +
		       "n.views, " +
		       "(select count(c) from CommentEntity c where c.notice.noticeid = n.noticeid)" +
		       ") " +
		       "from notice n join n.member m where n.member.id=:searchuserid "
		       + "and (:keyword is null or :keyword= '' or (:option='title' and n.title like %:keyword%) or (:option='content' and n.text like %:keyword%))",
		       countQuery = "select count(n) from notice n join n.member m where n.member.id=:searchuserid"
		       		+ " and (:userid is null or 1=1) "
		       		+  " and (:keyword is null or :keyword= '' or (:option='title' and n.title like %:keyword%) or (:option='content' and n.text like %:keyword%))") //이거패키지이름인데 notice는 Entity네임을 notice로함
	Page<TwitformnoticeDto> Userpagepost(@Param("searchuserid") Long searchuserid,@Param("userid") Long userid,Pageable pageable,String option,String keyword);
	
	//유저페이지 이미지리스트
	@Query(value="select n.id,n.title,m.username,m.nickname,m.profileimg,d.path,n.red, "
			+ "(select count(*) from detachfiles f where f.notice_id=n.id) AS file_count, "
			+ "(select count(*) from favorite_entity l where l.noticeid=n.id) AS like_count,n.views "
			+ "from notice n "
			+ "join member m on n.member_id =m.id "
			+ "join detachfiles d on n.id =d.notice_id "
			+ "where d.id= (select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
			+ "and n.member_id = :userid",
			countQuery = "select count(*) from notice n join detachfiles d on n.id=d.notice_id "
					+ "where d.id=(select MIN(d2.id) from detachfiles d2 where d2.notice_id=n.id) "
					+ "and n.member_id=:userid",
			nativeQuery = true)
	Page<Object[]> userpageimagelist(Long userid ,Pageable page);
}
