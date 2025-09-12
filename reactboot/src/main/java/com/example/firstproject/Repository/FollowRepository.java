package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Dto.follow.FollowerDto;
import com.example.firstproject.Dto.follow.followlistDto;
import com.example.firstproject.Entity.follow.FollowEntity;

@Repository

public interface FollowRepository extends JpaRepository<FollowEntity,Long>{
	//여기멤버가 객체이기때문에 따로필요한값을.id등으로 표현해줘야함
	@Query("select f from FollowEntity f where f.frommember.id=:from and f.tomember.id=:to")
	Optional<FollowEntity> checkfollow(@Param("from")Long followid, @Param("to") Long followerid);
	
	@Query("select f from FollowEntity f where f.tomember.id=:to")
	List<FollowEntity> findBytoMember( @Param("to") Long userid);

	@Query("select new com.example.firstproject.Dto.follow.followlistDto"
			+ "(m.username,m.nickname,f.favorite,m.profileimg,m.profileid) "
			+ "from FollowEntity f join f.tomember m "
			+ "where f.frommember.id=:from")
	List<followlistDto> findByFromMember(@Param("from")Long userid);
	
	@Query("select new com.example.firstproject.Dto.follow.FollowerDto "
			+ "(f.frommember.username,f.frommember.nickname,f.frommember.profileimg,f.frommember.profileid,"
			+ "CASE WHEN mf.id IS NOT NULL THEN true ELSE false END) "
			+ "FROM FollowEntity f " //이게 나를 팔로우한목록
			+ "LEFT JOIN FollowEntity mf ON mf.frommember.id=:from and " //이게 내가 팔로우한목록
			+ "mf.tomember.id=f.frommember.id"//이부분이 내가 팔로우한애가 지금나를팔로우했는가
			+ " where f.tomember.id=:from")//즉 나를 팔로우한 id 들 중에서
	List<FollowerDto> findfollowerlist(@Param("from")Long userid);
	
	@Query("select f from FollowEntity f where f.frommember.id=:from and f.favorite =true")
	List<FollowEntity> findBytoMemberandfavorite(@Param("from") Long userid);

	//두개안되나..걍네이티브씀
	@Query("select f from FollowEntity f where f.frommember.id=:userid Or f.tomember.id=:userid")
	List <FollowEntity> findByTomemberOrfindByFrommember(Long userid);

}
