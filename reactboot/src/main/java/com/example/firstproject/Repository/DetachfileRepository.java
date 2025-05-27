package com.example.firstproject.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.firstproject.Entity.detachfile;


public interface DetachfileRepository extends JpaRepository<detachfile,Long>{

	List<detachfile> findByPathContaining(String path);

	//노티스엔티티의 필드명을 사용하자
	List<detachfile> findByNotice_Noticeid(Long noticeid);
}
