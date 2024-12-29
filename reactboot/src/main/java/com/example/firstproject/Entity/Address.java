package com.example.firstproject.Entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable  //임베디드
@NoArgsConstructor
public class Address {

	@Column(name="adress")
	private String juso;
	
	

	private String gridx;
	
	private String gridy;

	@Builder
	public Address(String juso, String gridx, String gridy) {
		super();
		this.juso = juso;
		this.gridx = gridx;
		this.gridy = gridy;
	}
	

}
