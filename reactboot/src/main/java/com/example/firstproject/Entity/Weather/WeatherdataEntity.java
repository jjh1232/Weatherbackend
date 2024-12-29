package com.example.firstproject.Entity.Weather;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table(name="weatherregion")
public class WeatherdataEntity {

	@Id
	private String areacode;
	
	private String step1;
	
	private String step2;
	
	private String step3;
	
	private String gridx;
	
	private String gridy;
	
	private String longitudehour;
	
	private String longitudemin;
	
	private String longitudesec;
	
	private String latitudehour;
	
	private String latitudemin;
	
	private String latitudesec;
	
	private String longitudems;
	
	private String latitudems;
	
}
