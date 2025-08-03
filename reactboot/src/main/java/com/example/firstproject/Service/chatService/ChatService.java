package com.example.firstproject.Service.chatService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import com.example.firstproject.Dto.MemberDto;
import com.example.firstproject.Dto.MessageDto;
import com.example.firstproject.Dto.ChatDto.ChatResponseDto;
import com.example.firstproject.Dto.ChatDto.ChatdataDto;
import com.example.firstproject.Dto.ChatDto.RoomlistDto;
import com.example.firstproject.Dto.ChatDto.roomlistresponseDto;
import com.example.firstproject.Dto.ChatDto.stompchatDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.ChatlistmemberDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.EzRoomDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.EzmemberDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.MeseageDto;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdata;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roomdatainfo;
import com.example.firstproject.Dto.ChatDto.Roomdata.Roominfo;
import com.example.firstproject.Dto.ChatDto.Roomdata.RoommetaInfo;
import com.example.firstproject.Entity.MemberEntity;
import com.example.firstproject.Entity.StompRoom.MemberRoom;
import com.example.firstproject.Entity.StompRoom.Room;
import com.example.firstproject.Entity.StompRoom.chatmessage;
import com.example.firstproject.Entity.UserChatrecord.LastReadId;
import com.example.firstproject.Entity.UserChatrecord.chatrecord;
import com.example.firstproject.Handler.MemberHandler;
import com.example.firstproject.Repository.MemberRepository;
import com.example.firstproject.Repository.roomrepo.ChatMessageRepository;
import com.example.firstproject.Repository.roomrepo.ChatRoomRepository;
import com.example.firstproject.Repository.roomrepo.LastchatreadRepository;
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
	
	private final LastchatreadRepository chatreadrepo;
	
	
	
	
	@Qualifier("redisTemplateString")
	private final RedisTemplate<String, String> Stringredistemplate;
	
	//오브젝트용
	@Qualifier("ObjectredisTemplate")
	private final RedisTemplate<String, Object> redistemplate;
	
	
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
	//=========================redis로 유저정보캐쉬에저장해서 사용할경우=============================
	
	 // 리스트에 메시지 저장
   // redisTemplate.opsForList().rightPush("chat:room:" + roomId, message);
    
    // 해시에 메타데이터 저장
   // redisTemplate.opsForHash().put("chat:meta", roomId, LocalDateTime.now());
	//return redisTemplate.opsForList().range("chat:room:" + roomId, 0, -1);
	
	//메세지큐로 채팅방과부하관리======================================================================
		private final Queue<chatmessage> messagequeue=new ConcurrentLinkedDeque<>();
		private static final int BATCH_SIZE=100;
		private static final int Quad_SIZE=50;
		@Scheduled(fixedDelay=5000)//5초마다 이거 로직더수정할수있을거같음
		//1분마다 체크해서 보내기 저장딜레이를줄이자
		public void checkmessage() {
			
			/* 대용량아니면사실.. 이건필요할때 따로생성
			if(messagequeue.size()>=Quad_SIZE) {
				batchSavemessage();
			}
			*/
			if(!messagequeue.isEmpty()) {
				batchSavemessage();
			}
		
		}
		
		
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
	//=======================================================================================
	//챗데이터 db에저장  배치처리로 할까 생각했는데 멤버엔티티를 불러와야하는시점에서 배치는별로 레디스로 처리해보자
	//프론트에서 dto정보만 받으면 되긴하니까 연습용으로 배치도 가능할듯함
		
		
		private static final String keypre="memberentity:";
		private static final String roomkeypre="chatroomentity:";
	@Transactional
	public MeseageDto chatsave(Long roomid,stompchatDto mdto) throws IllegalAccessException {
		log.info("디비저장서비스");
		//레디스캐시에서먼저조회 근데 본체는 연관관계떄매 저장이 어려움 때문에 Ez멤버로
		String memberkey=keypre+mdto.getSender().getEmail();//유저아이디는안보냄
		String roomkey=roomkeypre+roomid;
		EzmemberDto member=(EzmemberDto) redistemplate.opsForValue().get(memberkey);
		EzRoomDto room=(EzRoomDto) redistemplate.opsForValue().get(roomkey);
		if(member ==null) {
			System.out.println("캐시에없어");
		MemberEntity memberEntity=memberrepo.findByUsername(mdto.getSender().getEmail()).orElseThrow(()->new IllegalAccessException("회원없음"));
		member=EzmemberDto.builder()
				.email(memberEntity.getUsername())
				.nickname(memberEntity.getNickname())
				.profileurl(memberEntity.getProfileimg())
				.userid(memberEntity.getId())
				.build();
		
		}
		if (room ==null) {
			Room roomEntity=roomrepo.findById(roomid).orElseThrow();
			room = EzRoomDto.builder().roomid(roomEntity.getId())
					.roomname(roomEntity.getRoomname())
					.build();
		}
		
		//레디스저장 키와 멤버값 시간두개인듯?
		redistemplate.opsForValue().set(memberkey, member,1800,TimeUnit.SECONDS);
		/*이후 레디스업데이트로직도 참고
		 // 캐시 무효화
        String redisKey = KEY_PREFIX + mdto.getEmail();
        redisTemplate.delete(redisKey);
		*/
		
		redistemplate.opsForValue().set(roomkey, room,1800,TimeUnit.SECONDS);
		
		//프록시객체가져오기 실제로셀렉트안가져옴	
		//실제db는아이디값만저장되니까 
		MemberEntity froxymember=memberrepo.getReferenceById(member.getUserid());
		Room froxyroom=roomrepo.getReferenceById(room.getRoomid());
		chatmessage save=chatmessage.builder()
				.member(froxymember)
				.sender(member.getNickname())
				.MessageType(mdto.getMessageType())
				.message(mdto.getMessage())
				.room(froxyroom)
				
				.build();
		
		
		//jpa가 save시에 영속성컨텍스트에 날짜가생겨서 batch를사용하려면 직접설정
		save.setCreatedDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd/HH:mm:ss")));
		messagerepo.save(save);
		
		MeseageDto dto=MeseageDto.builder()
				.chatid(save.getId())
				.roomid(room.getRoomid())
				.message(save.getMessage())
				.messagetype(save.getMessageType())
				.red(save.getCreatedDate())
				.sender(EzmemberDto.builder().email(member.getEmail())
						.nickname(member.getNickname())
						.profileurl(member.getProfileurl())
						.userid(member.getUserid())
						.build()
						)
				.build();
				
		
		//메세지큐로해보자
		//messagerepo.save(save);
		/* 메세지큐에적재후 인데 이거 실시간갱신하려면 id값이 필요해서그냥바로 세이브
		messagequeue.offer(save); 
		  if (messagequeue.size() >= BATCH_SIZE) {
			  batchSavemessage();
	        }
	        */
		//메세지큐에저장
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
	//방정보 데이터 멤버와 룸아이디같은거
	public List<Roomdatainfo> chatlistinfo(Long memberid){
		//이거새로만ㄷ든 아이디만가져오기
		//List<Long> memberroomids=memberroomrepo.findmemberroomidbymemberids(memberid);
		//생각해보니 이거 제목도필요하고 해서.. 걍전체가져오기
		List<MemberRoom> roomlist=memberroomrepo.findmemberroomlist(memberid);
		//룸아이디만추출
		List<Long> roomids=roomlist.stream().map(mr->mr.getRoom().getId()).collect(Collectors.toList());
		//각룸마다 멤버들추출
		//방별멤버가져오기
		
		List<ChatlistmemberDto> roominmemberlists=memberroomrepo.findmemberroomsbyroomids(roomids);	
		//방아이디와타이틀매칭
		Map<Long,String> roomidtotitle =roomlist.stream()
				.collect(Collectors.toMap(mr->mr.getRoom().getId(),mr->mr.getRoomname()));
		//방별멤버룸아이디도 받아서 그룹핑
		Map<Long,List<ChatlistmemberDto>> roommembermap=roominmemberlists.stream()
				.collect(Collectors.groupingBy(ChatlistmemberDto::getRoomid));
		
		List<Roomdatainfo> result=roomids.stream().map(roomid ->{
			String title=roomidtotitle.get(roomid);
			List<ChatlistmemberDto> members = roommembermap.getOrDefault(roomid,
					Collections.emptyList());
			
			return Roomdatainfo.builder().roomid(roomid).roomtitle(title)
					.membercount(members.size()).members(members).build();
		}).collect(Collectors.toList());
		
		return result;
	}
	//마지막채팅과 안읽은 메세지데이터
	public List<RoommetaInfo> chatlistsub(Long memberid,List<Long> roomids){
		//유저가 각방 마지막으로 읽은 챗아이디 아래조인으로해결해서필요없음
		//List<chatrecord> lastchatdata=chatreadrepo.findlastchatIds(memberid,roomids);
		
		//마지막채팅과 안읽은 챗데이터
		List<RoommetaInfo> meta=messagerepo.findLastMessageAndUnreadcount(memberid, roomids);
		
		
		return meta;
		
	}
	
	
	
	//멤버채팅창리스트 sql두번해서가져오기
	public List<RoomlistDto> findmemberlist(Long memberid){
		//멤버룸에서 룸아이디가져오기
		List<MemberRoom> roomlist=memberroomrepo.findmemberroomlist(memberid);
		//룸아이디만추출
		List<Long> roomids=roomlist.stream().map(mr->mr.getRoom().getId()).collect(Collectors.toList());
		//각룸마다 멤버들추출
		
		//멤버룸으로 멤버들가져옴
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
	
	//이거 컬렉션형을 fetchjoin으로 두번가져오면 카디널뭐였더라 그거 실행됨 
	//dto프로덕션쓰거나해얗나ㅡㄴ데 룸데이터도 같이 가져오는거 손해같아서 수정
	
	//확인필요
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
				c->MeseageDto.builder().chatid(c.getId())
				.roomid(c.getRoom().getId())
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
		//중복확인 -> 중복댐fetchjoin문제
		System.out.println("챗데이터중복확인");
		room.getChatdata().forEach(c -> System.out.println(c.getId() + " / " + c.getMessage()));
		return Roomdata.builder().roomid(room.getId())
				.roomname(room.getRoomname())
				.createred(room.getCreatedDate())
				.memberlist(memberlist)
				.chatdata(chatdatas)
				.build();
	}
	
	//위의 구조에서 수정
	//룸데이터인포
	public Roominfo roominfoget(Long roomid) {
		Room room =roomrepo.Roomdetailinfo(roomid);
		//필요한 정보만 가져오는게나은거같아스
		List<EzmemberDto> memberlist=room.getUserlist().stream()
				.map(ul-> EzmemberDto.builder()
						.userid(ul.getMember().getId())
						.email(ul.getMember().getUsername())
						.nickname(ul.getMember().getNickname())
						.profileurl(ul.getMember().getProfileimg())
						.build()
						
						
						).collect(Collectors.toList());
		return Roominfo.builder().roomid(room.getId())
				.roomname(room.getRoomname())
				.createred(room.getCreatedDate())
				.memberlist(memberlist)
				
				.build();
	}
	//채팅가져오기
	//수정필요
	public ChatdataDto chatdataget(Long roomid,Long userid){
		List<chatmessage> messagelist=messagerepo.Roomdetailchatget(roomid);
		
		//마지막 채팅 id 가져오기
		Long lastMessageid=messagelist.isEmpty() ? null: messagelist.get(messagelist.size()-1).getId();
		
		if(lastMessageid != null) {
			String rediskey="stomp:chat:lastread:roomid:"+roomid+":userid:"+userid;
			Stringredistemplate.opsForValue().set(rediskey, lastMessageid.toString());
		}
		
		//내가 이전에 마지막으로 읽은 메세지 
		//복합키라이게편함
		LastReadId key=LastReadId.builder().roomid(roomid).userid(userid).build();
		Optional<chatrecord> beforereadmessage=chatreadrepo.findById(key);
		//맵으로 값이 있을때만쓸수있음
		Long beforereadmessageid=beforereadmessage.map(chatrecord::getLastchatid)
				.orElse(lastMessageid);
		
		List<MeseageDto> chatdatas=messagelist.stream().map(
				c->MeseageDto.builder().chatid(c.getId())
				.roomid(roomid)
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
		
		
		 return new ChatdataDto(chatdatas,beforereadmessageid);
	}
	
}
