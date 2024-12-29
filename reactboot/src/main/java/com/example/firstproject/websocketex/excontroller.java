package com.example.firstproject.websocketex;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

//@ServerEndpoint(value="/open/chatex/{username}")
//@Component
public class excontroller {

	
	static HashMap<String,Session> messageUserList=new HashMap<String,Session>();
	
	
	@OnOpen //웹소켓 서버접속시 
	public void onOpen(Session session,@PathParam("username") String username) {
		//PathParam 은 웹소켓에서 사용하는 pathvariables임 
		
		//username 중복화인
		
		if(messageUserList.get(username) !=null) {
			System.out.println("아이디중복");
			sendMsg(session,"사용중인아이디입니다");
		
		}
		else {
			//중복이아닐경우 
			System.out.println("중복아님");
			messageUserList.put(username, session);
			 broadCast(username +"님이 입장 하셨습니다."  /* 현재 접속자 수 : +userList.size()*/ + "\n");
		}
		
	}
	
	@OnClose //서버정료시
	public void onClose(Session session) {
		String quituser=session.getId(); //종료한세션아이디확인
		//셋으로 메세지리스트 키셋 세팅으로 키값에맞는세션을 찾는다
		Set<String> keys=messageUserList.keySet();
		for(String key :keys) {
			if(quituser.equals((messageUserList).get(key).getId())){
				System.out.println("종료하려는username:"+key);
				messageUserList.remove(key,session);
				System.out.println("현재접속자:"+messageUserList.size());
				broadCast(key+"님이나가셨습니다");
			}
		}
	}
	
    //메시지 수신시
    @OnMessage
    public void onMessage(String msg, Session session) {
        broadCast(msg);
    }
    
    //에러 발생시
    @OnError
    public void onError(Session session, Throwable e) {
        System.out.println("문제 세션 : "+ session);
        System.out.println(e.toString());
    }
    //메세지전체전송
    private void broadCast(String text) {
    	System.out.println("전달대상:"+messageUserList.size());
    	  Set<String>keys =  messageUserList.keySet();
          try {            
              for(String key : keys) {
                  System.out.println("key : "+key);
                  Session session = messageUserList.get(key);    
                  //getbasicremote가뭐지 
                  session.getBasicRemote().sendText(key+"님:"+text);
                  System.out.println(session.getId() + "ID!!!");
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
    }
    
    //한명에게 메시지 전달
    private void sendMsg(Session session, String msg) {
        try {
            session.getBasicRemote().sendText(msg);
        } catch (IOException e) {    
            e.printStackTrace();
        }
    }
}
