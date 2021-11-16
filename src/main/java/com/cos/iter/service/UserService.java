package com.cos.iter.service;

import com.cos.iter.config.auth.dto.LoginUser;
import com.cos.iter.config.hanlder.ex.MyUserIdNotFoundException;
import com.cos.iter.domain.follow.FollowRepository;
import com.cos.iter.domain.user.User;
import com.cos.iter.domain.user.UserRepository;
import com.cos.iter.web.dto.JoinReqDto;
import com.cos.iter.web.dto.UserProfilePostRespDto;
import com.cos.iter.web.dto.UserProfileRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
@Service
@Log4j2
public class UserService {
	
	@PersistenceContext
	private EntityManager em;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final AzureService azureService;

	@Transactional
	public void uploadProfile(LoginUser loginUser, MultipartFile file) {
		String imageFilename = "";

		try {
			imageFilename = azureService.uploadToCloudAndReturnFileName(file, "profile");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		User userEntity = userRepository.findById(loginUser.getId()).orElseThrow(new Supplier<MyUserIdNotFoundException>() {
			@Override
			public MyUserIdNotFoundException get() {
				return new MyUserIdNotFoundException();
			}
		});
		
		// 더티체킹
		userEntity.setProfileImage(imageFilename);
	}
	
	@Transactional
	public void modifyUser(User user) {
		// 더티 체킹
		User userEntity = userRepository.findById(user.getId()).orElseThrow(new Supplier<MyUserIdNotFoundException>() {
			@Override
			public MyUserIdNotFoundException get() {
				return new MyUserIdNotFoundException();
			}
		});
		userEntity.setName(user.getName());
		userEntity.setBio(user.getBio());
	}
	
	@Transactional(readOnly = true)
	public User getUser(LoginUser loginUser) {
		return userRepository.findById(loginUser.getId())
				.orElseThrow(new Supplier<MyUserIdNotFoundException>() {
					@Override
					public MyUserIdNotFoundException get() {
						return new MyUserIdNotFoundException();
					}
				});
	}

	/*
		return 0: 이상 없음
		return 1: 중복 값 존재
		return 2: 다른 에러
	 */
	public short register(JoinReqDto joinReqDto) {
		log.info("서비스 회원가입 들어옴");
		log.info("joinReqDto: " + joinReqDto);

		String encPassword = bCryptPasswordEncoder.encode(joinReqDto.getPassword());
		log.info("encPassword : " + encPassword);
		joinReqDto.setPassword(encPassword);

		try {
			userRepository.save(joinReqDto.toEntity());
		} catch (DataIntegrityViolationException dataIntegrityViolationException) {
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			return 2;
		}

		return 0;
	}
	
	// 읽기 전용 트랜잭션
	// (1) 변경 감지 연산을 하지 않음.
	// (2) isolation(고립성)을 위해 Phantom read 문제가 일어나지 않음.
	@Transactional(readOnly = true)
	public UserProfileRespDto memberProfile(int id, LoginUser loginUser) {
		int followerCount;
		int followingCount;
		boolean followState;
		
		User userEntity = userRepository.findById(id).orElseThrow(new Supplier<MyUserIdNotFoundException>() {
					@Override
					public MyUserIdNotFoundException get() {
						return new MyUserIdNotFoundException();
					}
				});
		
		// 1. 이미지들과 전체 이미지 카운트(dto받기)
		String queryString = "SELECT po.id, " +
				"(SELECT url FROM image im WHERE im.post_id = po.id AND im.sequence = 0) AS image_url," +
				"(SELECT count(*) FROM likes lk WHERE lk.post_id = po.id) AS like_count, " +
				"(SELECT count(*) FROM comment ct WHERE ct.post_id = po.id) AS comment_count " +
				"FROM post po " +
				"WHERE po.user_id = ? AND po.visible = 1 " +
				"ORDER BY create_date DESC";
		Query query = em.createNativeQuery(queryString, "UserProfilePostRespDtoMapping").setParameter(1, id);
		List<UserProfilePostRespDto> postsEntity = query.getResultList();

		int postCount = postsEntity.size();
		
		// 2. doFollow 수
		followerCount = followRepository.mCountByFollower(id);
		followingCount = followRepository.mCountByFollowing(id);
		
		// 3. doFollow 유무
		followState = followRepository.mFollowState(loginUser.getId(), id) == 1;
		
		// 4. 최종마무리
		return UserProfileRespDto.builder()
				.pageHost(id==loginUser.getId())
				.followState(followState)
				.followerCount(followerCount)
				.followingCount(followingCount)
				.postCount(postCount)
				.user(userEntity)
				.posts(postsEntity) // 수정완료(Dto만듬) (댓글수, 좋아요수)
				.build();
	}
}






