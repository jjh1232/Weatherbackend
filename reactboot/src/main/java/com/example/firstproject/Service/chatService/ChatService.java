package com.example.firstproject.Service.chatService;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.roomrepo.ChatMessageRepository;
import com.example.firstproject.Repository.roomrepo.ChatRoomRepository;
import com.example.firstproject.Repository.roomrepo.MemberRoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class ChatService {

	private final ChatMessageRepository messagerepo;
	
	private final ChatRoomRepository roomrepo;
	
	private final MemberRepository memberrepo;
	
	private final MemberRoomRepository memberroomrepo;
	
	@Transactional
	public Long createChatroom(String roomname,List<String> memberlist) {
		//1대1방이랑 3인이상방은 구조가 조금 다른듯하다?
		//1대1방은 중복검산데 여기서 하는것보다 db도분리하고 보낸는사람 받는사람 도 엔티티에 받아서 하는게 효율적일듯?
		//단체방은 그냥이렇게 ㄱㄱ
		
		log.info("일단멤버추가룸네임은안정했으면 유저이름합치자");
		//유저 아이디로 셋으로 저장하자 중복 안되야하니까
		
		
		Room chatroom=Room.builder()
				.roomname(roomname)
				.build();
		log.info("챗룸객체생성");
		//마지막에 챗룸저장하면 멤버룸에 cascade를 붙여야 챗룸이 영속화되서 저장되는데 이러면
		//이유는모르겠으나 두번 들어가게 되서 미리 챗룸 생성하고 하는걸로 변경;;
		Long roomid=roomrepo.save(chatroom).getId();
		
		for (String username:memberlist) {
			log.info("멤버리스트찾기시작"+username);
			
			MemberEntity member=memberrepo.findByUsername(username).orElseThrow();
		
			MemberRoom memberroom=MemberRoom.builder()
					.membernickname(member.getNickname())
					.member(member)
					.room(chatroom)
					.build();
			
			log.info("멤버룸중간객체생성");
			chatroom.adduserlist(memberroom);
			
			
			log.info("챗룸목록에추가");
			member.addchatroom(memberroom);
			log.info("맴버객체에챗룸추가");
			
			chatmessage enterchat=chatmessage.builder()
					//.sender(member.getNickname()) 두번처리해야해서 귀찮아서 
					.MessageType("Message")
					.message(member.getNickname()+"님이 입장하셨습니다!")
					.room(chatroom)
					.build();
			
			messagerepo.save(enterchat);
			
		}
		
		//Long roomid=roomrepo.save(chatroom).getId();
		
		//return roomrepo.save(chatroom).getId(); //생성후아이디구하기
		return roomid;
		
		
	}
	//챗데이터 db에저장 
	public ChatResponseDto chatsave(Long roomid,String sender,String messageType,String message) {
		log.info("디비저장서비스");
		
		Room room=roomrepo.findById(roomid).orElseThrow();
		chatmessage save=chatmessage.builder()
				.sender(sender)
				.MessageType(messageType)
				.message(message)
				.room(room)
				.build();
		
		messagerepo.save(save);
		
		
		
		ChatResponseDto dto=ChatResponseDto.builder()
				.writer(save.getSender())
				.messageType(save.getMessageType())
				.message(save.getMessage())
				.roomId(roomid)
				
				.build();
		
		
		return dto;
		
	}
	//비폴챗
	public List<ChatResponseDto> getbeforechat(Room room){
		
		List<ChatResponseDto> list=new ArrayList<>();
	
		for(chatmessage data:room.getChatdata()) {
			ChatResponseDto dto=ChatResponseDto.builder()
					.roomId(data.getId())
					.messageType(data.getMessageType())
					.writer(data.getSender())
					.message(data.getMessage())
					
					.build();
			list.add(dto);
		}
		
		return list;
	}
	
	//룸아이디 찾기
	@Transactional
	public Room findbychatroom(Long loomid) {
		
		Room room=roomrepo.findById(loomid).orElseThrow();
		
		return room;
	}
	//룸에서 유저 나가기
	//@Transactional
	public void roomuserexit(Room room,MemberEntity member) {
		//유저삭제
		//연관관계의주인이 중간테이블인데 이값을 지울려면 findby로 중간관계테이블가져와야할듯?
		
		MemberRoom memberroom=memberroomrepo.findByRoom_idAndMember_Id(room.getId(), member.getId()).orElseThrow();
		log.info("구한멤버룸"+memberroom.getId().toString());
		log.info("구한멤버룸"+memberroom.getMembernickname().toString());
		
		
		//유저가 나간것도 채팅으로저장
		chatmessage enterchat=chatmessage.builder()
				//.sender(member.getNickname()) 두번처리해야해서 귀찮아서 
				.MessageType("Message")
				.message(member.getNickname()+"님이 퇴장하셨습니다!")
				.room(room)
				.build();
		
		messagerepo.save(enterchat);
		
		
		//실수로 memberroom에 quesqade설정해서 다날라갈뻔 ㅇㅇ;;
		memberroomrepo.delete(memberroom);
		
		
	
	}
	
	//룸에 유저추가
	@Transactional
	public Room Roomadduser(List<String> users,Long roomid) throws Exception {
		
		List<MemberEntity> userlist=new ArrayList<>();
		Room room=roomrepo.findById(roomid).orElseThrow();
		
		List<String> usernamelist=new ArrayList<>();
		for(MemberRoom members:room.getUserlist()) {
			String name=members.getMember().getUsername();
			usernamelist.add(name);
		}
		
		
		for(String username:users)
		{
			log.info(username+"초대멤버시작");
			for(String exuser:usernamelist) {
				
				if(username.equals(exuser)) {
					log.info("기존에존재하는유저입니다!");
					throw new Exception("에러");
						
				}
				else {
					log.info("기존에존재하지않는 아이디입니다");
				}
			}
			
			
			MemberEntity entity=memberrepo.findByUsername(username).orElseThrow();
			userlist.add(entity);
			
			MemberRoom memberroom=MemberRoom.builder()
					.membernickname(entity.getNickname())
					.member(entity)
					.room(room)
					.build();
			//추가
			entity.addchatroom(memberroom);
			room.adduserlist(memberroom);
			chatmessage enterchat=chatmessage.builder()
					//.sender(member.getNickname()) 두번처리해야해서 귀찮아서 
					.MessageType("Message")
					.message(entity.getNickname()+"님이 입장하셨습니다!")
					.room(room)
					.build();
			
			messagerepo.save(enterchat);
			
		}
		
		
		for(MemberRoom namlist:room.getUserlist()) {
			log.info("룸유저:"+namlist.getMembernickname());
			
		}
		
		return room;
		
		
	
		
	}
	
	//룸데이터가져오기
	public roomlistresponseDto roomdataget(Room room) {
		// TODO Auto-generated method stub
		roomlistresponseDto dto = roomlistresponseDto.builder()
				.roomid(room.getId())
				.roomname(room.getRoomname())
				.namelist(room.getUserlist())
				.build();
		return dto;
	}
}
