package com.example.firstproject.Entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable  //임베디드
@NoArgsConstructor
public class Address {

	@Column(name="adress")
	@ColumnDefault("서울특별시")
	private String juso;
	
	
	@ColumnDefault("60")
	private String gridx;
	@ColumnDefault("127")
	private String gridy;

	@Builder
	public Address(String juso, String gridx, String gridy) {
		super();
		this.juso = juso;
		this.gridx = gridx;
		this.gridy = gridy;
	}
	

}
