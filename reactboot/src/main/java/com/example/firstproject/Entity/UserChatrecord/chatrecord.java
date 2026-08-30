package com.example.firstproject.Entity.UserChatrecord;

import java.time.LocalDateTime;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name="lastreadchat")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class chatrecord {

	@EmbeddedId //엔티티에서 복합키 객체를 한번에 키로등록
	private LastReadId id; //복합키
	
	private Long lastchatid;
	
	
	private LocalDateTime lastdate;
	
	@PreUpdate
	public void preUpdate() {
		this.lastdate=LocalDateTime.now(); //업데이트시자동갱신
	}
	
	@PrePersist
	public void prepersist() {
		this.lastdate=LocalDateTime.now(); //인설트시자동갱신
	}
}
