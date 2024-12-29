package com.example.firstproject.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD}) //어노테이션이 생성될수 있는위치 선언
@Retention(RetentionPolicy.RUNTIME)
public @interface NoLogging {

}
