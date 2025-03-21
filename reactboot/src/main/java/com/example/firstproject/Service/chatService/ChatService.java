package com.example.firstproject.Service.chatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.MessageDto;
import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.RoomlistDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.ChatDto.stompchatDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.MeseageDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Handler.MemberHandler;
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
					.roomname(roomname)
					.build();
			
			log.info("멤버룸중간객체생성");
			chatroom.adduserlist(memberroom);
			
			
			log.info("챗룸목록에추가");
			member.addchatroom(memberroom);
			log.info("맴버객체에챗룸추가");
			Long a=(long) 43;
			MemberEntity System=memberrepo.findById(a).orElseThrow();
			chatmessage enterchat=chatmessage.builder()
					.sender("System") //이렇게설정할까 
					.member(System)//시스템일경우
					.MessageType("System")
					.message(member.getNickname()+"님이 입장하셨습니다!")
					.room(chatroom)
					.build();
			
			messagerepo.save(enterchat);
			
		}
		
		//Long roomid=roomrepo.save(chatroom).getId();
		
		//return roomrepo.save(chatroom).getId(); //생성후아이디구하기
		return roomid;
		
		
	}
	
	//메세지큐======================================================================
		private final Queue<chatmessage> messagequeue=new ConcurrentLinkedDeque<>();
		private static final int BATCH_SIZE=100;
		
		@Scheduled(fixedDelay=50000)//5초마다 이거 로직더수정할수있을거같음
		public void batchSavemessage() {
			List<chatmessage> messagesave=new ArrayList<>();
			chatmessage message;
			while((message= messagequeue.poll())!=null&&messagesave.size()<BATCH_SIZE) {
				//꺼냇는데 널이아니거나 사이즈가배치보다 작을경우
				messagesave.add(message);
			}
			if(!messagesave.isEmpty()) {
				//메세지저장
				messagerepo.saveAll(messagesave);
			}
		}
	//챗데이터 db에저장  배치처리로 할까 생각했는데 멤버엔티티를 불러와야하는시점에서 배치는별로 레디스로 처리해보자
	//프론트에서 dto정보만 받으면 되긴하니까 연습용으로 배치도 가능할듯함
	public MeseageDto chatsave(Long roomid,stompchatDto mdto) throws IllegalAccessException {
		log.info("디비저장서비스");
		
		Room room=roomrepo.findById(roomid).orElseThrow();
		MemberEntity member=memberrepo.findByUsername(mdto.getSender().getEmail()).orElseThrow(()->new IllegalAccessException("회원없음"));
		
		chatmessage save=chatmessage.builder()
				.member(member)
				.sender(member.getNickname())
				.MessageType(mdto.getMessageType())
				.message(mdto.getMessage())
				.room(room)
				
				.build();
		
		//jpa가 save시에 영속성컨텍스트에 날짜가생겨서 batch를사용하려면 직접설정
		save.setCreatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:ss")));
	
		
		MeseageDto dto=MeseageDto.builder()
				.message(save.getMessage())
				.messagetype(save.getMessageType())
				.red(save.getCreatedDate())
				.sender(EzmemberDto.builder().email(member.getUsername())
						.nickname(member.getNickname())
						.profileurl(member.getProfileimg())
						.userid(member.getId())
						.build()
						)
				.build();
				
		//메세지큐로해보자
		//messagerepo.save(save);
		messagequeue.offer(save); //메세지큐에저장
		/*
		ChatResponseDto dto=ChatResponseDto.builder()
				.userprofile(member.getProfileimg())
				.writer(member.getNickname())
				.messageType(save.getMessageType())
				.message(save.getMessage())
				.roomId(roomid)
				.red(save.getCreatedDate())
				.build();
		
		*/
		return dto;
		
	}
	//비폴챗
	public List<ChatResponseDto> getbeforechat(Room room){
		
		List<ChatResponseDto> list=new ArrayList<>();
	
		for(chatmessage data:room.getChatdata()) {
			ChatResponseDto dto=ChatResponseDto.builder()
					.roomid(data.getId())
					.messageType(data.getMessageType())
					//.userprofile(data.getMember().getProfileimg())
					//.writer(data.getSender())
					.message(data.getMessage())
					.red(data.getCreatedDate())
					.build();
			list.add(dto);
		}
		
		return list;
	}
	
	//룸아이디 찾기
	@Transactional
	public Room findbychatroom(Long loomid) {
		//이거 join으로쓰자
		Room room=roomrepo.findById(loomid).orElseThrow();
		
		return room;
	}
	
	public Roomdata Roomdataget(Long roomid) {
		Room room=roomrepo.findbyroomdata(roomid);
		//멤버데이터가져오기
		List<EzmemberDto> memberlist=room.getUserlist().stream()
				.map(ul-> EzmemberDto.builder()
						.userid(ul.getMember().getId())
						.email(ul.getMember().getUsername())
						.nickname(ul.getMember().getNickname())
						.profileurl(ul.getMember().getProfileimg())
						.build()
						
						
						).collect(Collectors.toList());
		
		List<MeseageDto> chatdatas=room.getChatdata().stream().map(
				c->MeseageDto.builder().id(c.getId())
				.messagetype(c.getMessageType())
				.message(c.getMessage())
				.red(c.getCreatedDate())
				.sender(EzmemberDto.builder()
						.userid(c.getMember().getId())
						.email(c.getMember().getUsername())
						.nickname(c.getMember().getNickname())
						.profileurl(c.getMember().getProfileimg())
						.build())
				.build()
				)
				.collect(Collectors.toList());
		
		return Roomdata.builder().roomid(room.getId())
				.roomname(room.getRoomname())
				.createred(room.getCreatedDate())
				.memberlist(memberlist)
				.chatdata(chatdatas)
				.build();
	}
	//룸에서 유저 나가기
	//@Transactional
	public void roomuserexit(Room room,MemberEntity member) {
		//유저삭제
		//연관관계의주인이 중간테이블인데 이값을 지울려면 findby로 중간관계테이블가져와야할듯?
		
		MemberRoom memberroom=memberroomrepo.findByRoom_idAndMember_Id(room.getId(), member.getId()).orElseThrow();
		log.info("구한멤버룸"+memberroom.getId().toString());
		log.info("구한멤버룸"+memberroom.getMembernickname().toString());
		
	
		//유저가 나간것도 채팅으로저장 시스템 멤버가져오기 
		//근데매번 Db에 가는것도 비효율적인거같긴해서 그냥내가 생성할까
		Long a=(long) 43;
		MemberEntity System=memberrepo.findById(a).orElseThrow();
		
		chatmessage enterchat=chatmessage.builder()
				.sender("System") //이렇게설정할까 
				.member(System)//시스템일경우
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
			Long a=(long) 43;
			MemberEntity System=memberrepo.findById(a).orElseThrow();
	
			chatmessage enterchat=chatmessage.builder()
					.sender("System") //이렇게설정할까 
					.member(System)//시스템일경우
					.MessageType("System")
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
	//멤버정보가져오기 
	public List<MemberRoom> findbyuserchatroom(Long memberid) {
		List<MemberRoom> memberroom=memberroomrepo.findMemberrooms(memberid);
		
		return memberroom;
		
	}
	
	//멤버정보 sql두번해서가져오기
	public List<RoomlistDto> findmemberlist(Long memberid){
		
		List<MemberRoom> roomlist=memberroomrepo.findmemberroomlist(memberid);
		
		List<Long> roomids=roomlist.stream().map(mr->mr.getRoom().getId()).collect(Collectors.toList());
		
		List<MemberRoom> allmemberrooms=memberroomrepo.findMemberRoomsbyroomid(roomids);
		
		//방별멤버생성
		Map<Long,List<MemberDto>> roommembermap=allmemberrooms.stream()
				//스트림을 특정요소기준에따라 그룹화하여map반환
				.collect(Collectors.groupingBy(mr->mr.getRoom().getId(),
						//원하는요소로맵핑
						Collectors.mapping(mr-> new MemberDto(mr.getMember())
								,Collectors.toList())));
		
		//방별 메세지생성
		Map<Long, Map<String, Object>> roomMessageInfo = roomlist.stream()
			    .collect(Collectors.toMap(
			        mr -> mr.getRoom().getId(),
			        mr -> {
			            List<chatmessage> messages = mr.getRoom().getChatdata(); // 룸 객체에서 메시지 리스트 가져오기
			            chatmessage lastmessage=messages.isEmpty()?null:
			            	messages.get(messages.size()-1);
			           
			            Map<String,Object> info =new HashMap<>();
			            info.put("lastmessage", lastmessage);
			            info.put("totalmessage",messages.size());
			            return info;
			        }
			        ));
		
			  return roomlist.stream().map(mr->{
				  Long roomid=mr.getRoom().getId();
				  Map<String,Object> messageinfo=roomMessageInfo.get(roomid);
				 
				  return new RoomlistDto(
							mr.getRoom(),
							roommembermap.get(mr.getRoom().getId()),
							mr.getRoomname(),
							(chatmessage) messageinfo.get("lastmessage"),
							(int) messageinfo.get("totalmessage")
							);
				  }).collect(Collectors.toList());
			  
	}
}
