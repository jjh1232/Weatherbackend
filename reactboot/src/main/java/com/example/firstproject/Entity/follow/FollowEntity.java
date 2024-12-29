package com.example.firstproject.Entity.follow;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.firstproject.Entity.MemberEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
public class FollowEntity {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private long id;

    @ManyToOne
    @JoinColumn(name = "frommember")
    private MemberEntity frommember;

    @ManyToOne
    @JoinColumn(name = "tomember")
    private MemberEntity tomember;
    
    @ColumnDefault("false")
    private boolean favorite;
    
    @CreatedDate //@LastModifiedDate이건 업데이트기준 크리에이트는 그냥 엔티티생성시
    private LocalDateTime times;

   
}
