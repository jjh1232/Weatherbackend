package com.example.firstproject.Entity.UserChatrecord;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable //복합키객체를 한번에키로등록
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode //복합키 비교와 해시처리를 정확히하기위해 필수라고함
@AllArgsConstructor
@Builder
public class LastReadId implements Serializable {//복합키는 내부적으로 직렬화가 꼭필요하다

	@Column(name="userid")
	private Long userid;
	
	@Column(name="roomid")
	private Long roomid;
}
