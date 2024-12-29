package com.example.firstproject.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD}) //어노테이션이 생성될수 있는위치 선언
@Retention(RetentionPolicy.RUNTIME)
public @interface Logoutano {

	/*
	 * 타겟에는 package constructor 같은 여러 가지 있다
	 * */
	
	/* Retention 어노테이션이 언제까지 유효할지 정하는것
	 * RUNTIME -컴파일 이후에도 참조가능 ->실행동안 계속유지 
	 * CLASS = 클래스를 참조할때까지 유효 ->
	 * SOURCE=컴파일이후 어노테이션 정보소멸 ->컴파일 이후 어노테이션 정보 소멸
	 * 
	 * */
}
