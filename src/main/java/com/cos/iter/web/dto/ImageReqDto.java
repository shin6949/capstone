package com.cos.iter.web.dto;

import com.cos.iter.domain.post.Post;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ImageReqDto {
	private List<MultipartFile> file;
	private String content;
	private List<Float> latitude;
	private List<Float> longitude;
	private List<String> locationName;
	private List<String> roadAddress;
	private List<String> kakaoMapUrl;

	public Post toPostEntity() {
		return Post.builder()
				.content(content)
				.build();
	}
}





