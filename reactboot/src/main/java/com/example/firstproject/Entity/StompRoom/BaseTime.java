package com.example.firstproject.Entity.StompRoom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@MappedSuperclass
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTime {

	
	@CreatedDate
    @Column(updatable = false)
    private String createdDate;

    @LastModifiedDate
    @Column(updatable = true)
    private String updatedDate;
    
    
   @PrePersist
   public void onpersist() {
	   this.createdDate=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:s"));
	   this.updatedDate=this.createdDate;
   }
  
    @PreUpdate
    public void onpreupdate() {
    	this.updatedDate=LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:ss"));
    }
    
     
}
