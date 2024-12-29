package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.follow.FollowEntity;

@Repository
public interface FollowRepository extends JpaRepository<FollowEntity,Long>{
	//여기멤버가 객체이기때문에 따로필요한값을.id등으로 표현해줘야함
	@Query("select f from FollowEntity f where f.frommember.id=:from and f.tomember.id=:to")
	Optional<FollowEntity> checkfollow(@Param("from")Long followid, @Param("to") Long followerid);
	
	@Query("select f from FollowEntity f where f.tomember.id=:to")
	List<FollowEntity> findBytoMember( @Param("to") Long userid);

	@Query("select f from FollowEntity f where f.frommember.id=:from")
	List<FollowEntity> findByFromMember(@Param("from")Long userid);
	
	@Query("select f from FollowEntity f where f.frommember.id=:from and f.favorite =true")
	List<FollowEntity> findBytoMemberandfavorite(@Param("from") Long userid);

}
