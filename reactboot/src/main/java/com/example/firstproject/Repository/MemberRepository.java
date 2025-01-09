package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.MemberEntity;

@Repository

public interface MemberRepository extends JpaRepository<MemberEntity, Long>{

	
	Optional<MemberEntity> findByrefreshtoken(String refreshtoken);
	
	@Query(value="Select Count(username) from member where username =:username" ,nativeQuery=true)
	Long emailcheck(@Param("username") String username);
	
	

	Optional<MemberEntity> findByUsername(String username);
	//executeQuery로 전송되기때문에 update,delete,insult문은 리턴값이없어 안됨 따라서 executeupdtq()로전송되는 modifying을사용
	@Modifying(clearAutomatically = true)//해당쿼리메서드실행직호 영속성컨텍스트를클리어할것인지아닌지 기본은디폴트임 
	@Transactional
	@Query(value="update member set password=:password where username=:username",nativeQuery=true)
	public void passwordupdate(@Param("username") String username,@Param("password") String password);



	List<MemberEntity> findByNicknameContaining(String keyword);
	
	Page<MemberEntity> findByUsernameContaining(Pageable page,String keyowrd);
	
	Page<MemberEntity> findByNicknameContaining(Pageable page,String keyword);
}
