package com.example.firstproject.Repository.History;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.LoginHistory;

@Repository

public interface LoginhistoryRepository extends JpaRepository<LoginHistory, Long> {

	List<LoginHistory> findByuseridOrderByLogindtDesc(String userId);

	Page<LoginHistory> findByUserid(String userid, Pageable pageable);

	Page<LoginHistory> findByUseridAndLogindtContaining(String userid, String logindt, Pageable pageable);
}
