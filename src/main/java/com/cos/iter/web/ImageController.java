package com.cos.iter.web;

import com.cos.iter.service.PostService;
import com.cos.iter.util.Logging;
import com.cos.iter.util.Script;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.cos.iter.config.auth.LoginUserAnnotation;
import com.cos.iter.config.auth.dto.LoginUser;
import com.cos.iter.service.ImageService;
import com.cos.iter.web.dto.ImageReqDto;

import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
@Controller
@Log4j2
public class ImageController {
	private final ImageService imageService;
	private final PostService postService;
	private final Logging logging;

	@Value("${azure.blob.url}")
	private String blobStorageUrl;

	@GetMapping("/image/uploadForm")
	public String imageUploadForm(Model model) {
		model.addAttribute("storageUrl", blobStorageUrl);

		return "image/new-image-upload";
	}
	
	@PostMapping("/image/upload")
	@Transactional
	public String imageUpload(@LoginUserAnnotation LoginUser loginUser, ImageReqDto imageReqDto) {
		log.info(logging.getClassName() + " / " + logging.getMethodName());
		log.info("ImageReqDto: " + imageReqDto);

		int postId = postService.saveAndReturnId(imageReqDto, loginUser.getId());
		log.info("Inserted Post Id: " + postId);

		try {
			imageService.contentUpload(imageReqDto, postId);
		} catch (Exception e) {
			e.printStackTrace();
			return Script.alert("업로드 과정에서 문제가 발생하였습니다.");
		}

		return "redirect:/user/" + loginUser.getId();
	}
}




