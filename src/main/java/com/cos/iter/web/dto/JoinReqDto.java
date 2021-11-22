package com.cos.iter.web.dto;

import com.cos.iter.domain.user.User;
import com.cos.iter.domain.user.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinReqDto {
	private String email;
	private String name;
	private String username;
	private String password;
	private String passwordRe;
	
	public User toEntity() {
		return User.builder()
				.email(email)
				.name(name)
				.username(username)
				.password(password)
				.role(UserRole.USER)
				.build();
	}
}
