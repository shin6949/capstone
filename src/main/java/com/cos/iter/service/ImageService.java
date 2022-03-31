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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Log4j2
public class ImageService {
	private final ImageRepository imageRepository;
	private final TagRepository tagRepository;
	private final AzureService azureService;
	private final PostRepository postRepository;
	private final TagParser tagParser;

	@Value("${file.upload.method}")
	private String fileUploadMethod;

	@Value("${file.temp-upload-path}")
	private String tempPath;

	@Transactional(rollbackFor = {Exception.class})
	public boolean photoUploadToCloud(ImageReqDto imageReqDto, int postId) throws IOException{
		final Post postEntity = postRepository.findById(postId).orElseThrow(null);

		for(short i = 0; i < imageReqDto.getFile().size(); i++) {
			final String imageFilename = writeFileAtTempPath(imageReqDto.getFile().get(i));

			// Image Meta Data Upload
			final Image image = Image.builder()
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
		}

		// Tag 저장 -> Tag는 Post에 종속되어 있으므로 한 번만 등록하면 됨.
		final List<String> tagNames = tagParser.tagParse(imageReqDto.getContent());
		log.info("tag: " + tagNames);
		for (String name : tagNames) {
			final Tag tag = Tag.builder()
					.post(postEntity)
					.name(name)
					.build();
			tagRepository.save(tag);
		}

		return true;
	}

	// 임시 파일 작성
	private String writeFileAtTempPath(MultipartFile file) throws IOException {
		final UUID uuid = UUID.randomUUID();
		final String generatedFileName = uuid + "_" + file.getOriginalFilename();
		final Path fullFilePath = Paths.get(tempPath + generatedFileName);

		Files.write(fullFilePath, file.getBytes());

		return generatedFileName;
	}

	// 임시 파일 삭제
	private void deleteFileAtTempPath(String fileName) throws IOException {
		// 결과에 무관하게 폐기 (실패 시에는 실패했다는 메시지가 Return 되므로)
		try {
			Files.delete(Path.of(tempPath + fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}