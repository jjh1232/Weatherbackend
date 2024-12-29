package com.example.firstproject.Entity;

import java.time.LocalDateTime;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="detachfiles")
public class detachfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	@Column
	public Long idx;//글내부아이디
	@Column
	public Long rangeindex; //글내부 데이터 위치를위한인덱스 
	
	@Column(nullable = false)
	public String filename;
	
	@Column(nullable = false)
	public String path;
	

	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="notice_id" )
	@JsonIgnore
	private NoticeEntity notice;
	
	
	
}
