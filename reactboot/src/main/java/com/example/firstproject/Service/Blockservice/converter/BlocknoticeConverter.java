package com.example.firstproject.Service.Blockservice.converter;

import java.util.Arrays;
import java.util.EnumSet;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.springframework.util.StringUtils;

import com.example.firstproject.Entity.block.BlockEnum.NoticeblockEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter //Entity에서사용할 x는 엔티티에사용할타입 db에읽어오거나뭐할떄 y로 string선언
public class BlocknoticeConverter implements AttributeConverter<EnumSet<NoticeblockEnum>, String> {

	//db에저장할 type으로 Entitytype을 변환하는로직
	@Override
		public String convertToDatabaseColumn(EnumSet<NoticeblockEnum> attribute) {
			// TODO Auto-generated method stub
			//스트링타입으로 사용하기위해 빌더선언
		StringBuilder sb=new StringBuilder();
		//스트림돌며 sb에 ,이거추가
		attribute.stream().forEach(e->sb.append(e.name()+","));
		//최종결과 String변환
		String result =sb.toString();
		//마지막 ,제거
		if(result.charAt(result.length()-1)==',') {
			result=result.substring(0,result.length()-1); //마지막은미만임
			//return result;
		}
			return result;
		}

	//Entity에서 사용할 type으로 Dbtype을 변환하는 로직
	@Override
	public EnumSet<NoticeblockEnum> convertToEntityAttribute(String dbData) {
		// TODO Auto-generated method stub
		// DB에서 읽어온 값이 null이거나 공백이거나 CATEGORY.KOREA(name="한식") 형태로 읽어올 경우 제외
		if(dbData==null||dbData==""||dbData.contains(".")) {
			return EnumSet.noneOf(NoticeblockEnum.class);
			
		}
		//최초빈컬렉션생성
		EnumSet<NoticeblockEnum> attribute=EnumSet.noneOf(NoticeblockEnum.class);
		//Db에서읽어온데이터 문자열,문자열 데이터를 ,로스플릿
		String [] dbDataArray=StringUtils.trimAllWhitespace(dbData).split(",");
		// 빈 Collection으로 생성한 EnumSet에 split한 data를 Category(Enum) .valueOf로 생성
        // 해당 구문에서 Enum에 선언되지 않은 값 존재 시 Exception 발생 가능
		Arrays.stream(dbDataArray).forEach(e->attribute.add(NoticeblockEnum.valueOf(e)));
		return attribute;
		
	}
}
