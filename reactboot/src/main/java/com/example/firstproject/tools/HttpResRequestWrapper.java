package com.example.firstproject.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.hibernate.engine.jdbc.StreamUtils;

//바디내용을 얻는건 한번하면 없어지기때문에 따로만든클래스
public class HttpResRequestWrapper extends HttpServletRequestWrapper{
	private byte[] bodyData;
	
	public HttpResRequestWrapper(HttpServletRequest request) {
		
		super(request);
		System.out.println("래퍼생성");
		// TODO Auto-generated constructor stub
		InputStream is;
		try {
			is = super.getInputStream();
			//input스트림의 모든 밥이트들을 읽음
			this.bodyData=is.readAllBytes();
			for(int i=0;i<bodyData.length;i++) {
			System.out.println("바디데이터:"+bodyData[i]);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	
	public ServletInputStream getInputStream() {
		
		ByteArrayInputStream bytearrayinputstream=new ByteArrayInputStream(this.bodyData);
		for(int i=0;i<bodyData.length;i++) {
			System.out.println("바디데이터:"+bodyData[i]);
			}
		ServletInputStream servletInputStream= new ServletInputStream() {
			
			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				return bytearrayinputstream.read();
			}
			
			@Override
			public void setReadListener(ReadListener listener) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}
		};
		return servletInputStream;
	}
	
	public ServletRequest getRequest() {
		return super.getRequest();
	}
}
