package com.example.firstproject.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.Notification;

@Repository

public interface NotificationRepository extends JpaRepository<Notification, Long>{

	List<Notification> findByMemberId(Long memberid, Pageable pageable);

	//커버링인덱스로 아이디만가져오면 값이 훨씨싸다고함
	@Query("select COUNT(n.id) FROM Notification n where n.member.id=:memberid")
	Long notificount(Long memberid);
	
}
