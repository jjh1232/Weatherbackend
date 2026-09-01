package com.example.firstproject.Entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable  //임베디드
@NoArgsConstructor
@Getter
@Builder
public class Address {

	@Column(name="adress")
	// @ColumnDefault 는 값을 SQL 에 그대로 붙여넣는다. 문자열이면 따옴표를 직접 넣어야 한다.
	// 없으면 "default 서울특별시 ..." 가 되어 create table 이 문법 오류로 실패한다.
	// (로컬은 member 테이블이 이미 있어서 ddl-auto:update 가 건너뛰느라 안 드러났다)
	@ColumnDefault("'서울특별시  종로구  청운효자동'")
	@NotBlank
	private String juso="서울특별시  종로구  청운효자동";
	 //빌더사용시 기본값이라는디
	
	@ColumnDefault("'60'")
	@NotBlank
	private String gridx="60";
	@ColumnDefault("'127'")
	@NotBlank
	private String gridy="127";

	
	public Address(String juso, String gridx, String gridy) {
		super();
		this.juso = juso;
		this.gridx = gridx;
		this.gridy = gridy;
	}
	

}
