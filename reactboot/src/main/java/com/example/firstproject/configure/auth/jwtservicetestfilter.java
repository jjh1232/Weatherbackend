package com.example.firstproject.configure.auth;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.firstproject.Service.JwtService;
import com.example.firstproject.Service.Memberservice.MemberService;

import lombok.RequiredArgsConstructor;
//일단이런방식도있다나두자 필터에추가는안했음 
@RequiredArgsConstructor
public class jwtservicetestfilter extends OncePerRequestFilter{

	private final JwtService jwtservice;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		System.out.println("흠필터가");
		String username="";
		
		
		UsernamePasswordAuthenticationToken authenticationToken=
				new UsernamePasswordAuthenticationToken(username,null,List.of(new SimpleGrantedAuthority("USER")));
		
		authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		
	}

}
