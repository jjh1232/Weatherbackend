package com.example.firstproject.Entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)//엔티티변화를감지해 매핑된테이블조작함
public class LoginHistory {//어디팅리스터너가 수정을감지함

	@Id
	@GeneratedValue
	private Long id;
	private String userid;
	
	@CreatedDate
	private LocalDateTime logindt; //로그인한시간날짜
	
	private boolean islogin;//로그인성공여부 
	
	private String clientip;
	private String userdata;
	
	
}
