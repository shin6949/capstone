package com.cos.iter.service;

import com.cos.iter.domain.image.Image;
import com.cos.iter.domain.image.ImageRepository;
import com.cos.iter.domain.post.Post;
import com.cos.iter.domain.post.PostRepository;
import com.cos.iter.domain.tag.Tag;
import com.cos.iter.domain.tag.TagRepository;
import com.cos.iter.enums.FileType;
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
	private final OnPremSaveService onPremSaveService;
	private final PostRepository postRepository;
	private final TagParser tagParser;

	@Value("${file.upload.method}")
	private String fileUploadMethod;

	@Value("${file.temp-upload-path}")
	private String tempPath;

	@Transactional(rollbackFor = {Exception.class})
	public void contentUpload(ImageReqDto imageReqDto, int postId) throws Exception {
		final Post postEntity = postRepository.findById(postId).orElseThrow(null);

		// 파일이 여러 개일 수 있기 때문에 for로 진행
		for(short i = 0; i < imageReqDto.getFile().size(); i++) {
			final String imageFilename = writeFileAtTempPath(imageReqDto.getFile().get(i));

			// Image 메타 데이터 Insert
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

			// 각각 지정한 방식에 따라 파일 저장
			uploadFileEachMethod(imageFilename, FileType.USER_CONTENT);

			// 임시 저장 파일 삭제
			deleteFileAtTempPath(imageFilename);
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
	}

	@Transactional(rollbackFor = {Exception.class})
	public String profileUpload(MultipartFile file) throws IOException {
		final String imageFilename = writeFileAtTempPath(file);

		// 각각 지정한 방식에 따라 파일 저장
		uploadFileEachMethod(imageFilename, FileType.USER_PROFILE);

		// 임시 저장 파일 삭제
		deleteFileAtTempPath(imageFilename);

		return imageFilename;
	}

	private void uploadFileEachMethod(String imageFilename, FileType fileType) throws IOException {
		if(fileUploadMethod.equals("ONPREMISE")) {
			onPremSaveService.saveFile(imageFilename, fileType);
		} else if(fileUploadMethod.equals("AZURE")) {
			azureService.uploadFile(imageFilename, fileType.getAzureContainerName());
		}
	}

	private String writeFileAtTempPath(MultipartFile file) throws IOException {
		final UUID uuid = UUID.randomUUID();
		final String generatedFileName = uuid + "_" + file.getOriginalFilename();
		final Path fullFilePath = Paths.get(tempPath + generatedFileName);

		Files.write(fullFilePath, file.getBytes());

		return generatedFileName;
	}

	// 임시 파일 삭제
	private void deleteFileAtTempPath(String fileName) {
		try {
			Files.delete(Path.of(tempPath + fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}