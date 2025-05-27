package com.example.firstproject.Dto.Previewimage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreviewimageDto {

	private Long id;
	private String filename;
	private String path;
}
