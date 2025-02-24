package com.example.firstproject.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto;
import com.example.firstproject.Entity.NoticeEntity;
import com.example.firstproject.Entity.block.NoticedecleEntity;

public interface NoticeDeclerepository  extends JpaRepository<NoticedecleEntity, Long>{

	//이거 노티스자체가필요한게 맘에안들어서 쿼리로대체
	@Query(value = "Select d from NoticedecleEntity d where d.notice.id=:noticeid")
	public Page<NoticedecleEntity> findbyNoticeid(Long noticeid,Pageable pageable);
	//@Query(value = "Select new com.example.firstproject.Dto.blockDto.Adminnoticedecleresponsedto(d.member.username,d.notice.id,d.reason,d.datetime) from NoticedecleEntity d where d.notice.id=:noticeid")
	//Dto로받긴데 왜안될까..
		
}
