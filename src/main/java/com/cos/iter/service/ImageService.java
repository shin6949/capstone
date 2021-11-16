package com.cos.iter.service;

import com.cos.iter.domain.image.Image;
import com.cos.iter.domain.image.ImageRepository;
import com.cos.iter.domain.post.Post;
import com.cos.iter.domain.post.PostRepository;
import com.cos.iter.domain.tag.Tag;
import com.cos.iter.domain.tag.TagRepository;
import com.cos.iter.util.TagParser;
import com.cos.iter.web.dto.ImageReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
@Log4j2
public class ImageService {
	private final ImageRepository imageRepository;
	private final TagRepository tagRepository;
	private final AzureService azureService;
	private final PostRepository postRepository;
	private final TagParser tagParser;

	@Transactional
	public void photoUploadToCloud(ImageReqDto imageReqDto, int postId) {
		final Post postEntity = postRepository.findById(postId).orElseThrow(null);

		for(short i = 0; i < imageReqDto.getFile().size(); i++) {
			try {
				final String imageFilename = azureService.uploadToCloudAndReturnFileName(imageReqDto.getFile().get(i), "photo");

				Image image = Image.builder()
						.post(postEntity)
						.latitude(imageReqDto.getLatitude().get(i))
						.longitude(imageReqDto.getLongitude().get(i))
						.locationName(imageReqDto.getLocationName().get(i))
						.roadAddress(imageReqDto.getRoadAddress().get(i))
						.kakaoMapUrl(imageReqDto.getKakaoMapUrl().get(i))
						.sequence(i)
						.url(imageFilename)
						.build();

				imageRepository.save(image);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Tag 저장 -> Tag는 Post에 종속되어 있으므로 한 번만 등록하면 됨.
		List<String> tagNames = tagParser.tagParse(imageReqDto.getContent());
		log.info("tag: " + tagNames);
		for (String name : tagNames) {
			Tag tag = Tag.builder()
					.post(postEntity)
					.name(name)
					.build();
			tagRepository.save(tag);
		}
	}
}