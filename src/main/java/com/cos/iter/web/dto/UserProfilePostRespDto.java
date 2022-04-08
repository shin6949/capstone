package com.cos.iter.web.dto;

import com.cos.iter.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfilePostRespDto {
	private int id;
	private String imageUrl;
	private int likeCount;
	private int commentCount;

	public String getImageUrl() {
		return FileType.USER_PROFILE.getUrl(imageUrl);
	}
}
