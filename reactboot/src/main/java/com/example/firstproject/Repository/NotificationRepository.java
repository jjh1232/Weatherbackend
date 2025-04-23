package com.example.firstproject.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.Notification;

@Repository

public interface NotificationRepository extends JpaRepository<Notification, Long>{

	Page<Notification> findByMemberId(Long memberid, Pageable pageable);

	
}
