package com.cos.iter.web.dto;

import com.cos.iter.domain.post.Post;
import org.springframework.web.multipart.MultipartFile;

import com.cos.iter.domain.image.Image;
import com.cos.iter.domain.user.User;

import lombok.Data;

@Data
public class ImageReqDto {
	private MultipartFile file;
	private float latitude;
	private float longitude;
	private String tags;
	
	public Image toEntity(String imageUrl, Post postEntity) {
		return Image.builder()
				.latitude(latitude)
				.longitude(longitude)
				.url(imageUrl)
				.post(postEntity)
				.build();
	}
}





