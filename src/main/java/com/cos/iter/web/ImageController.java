package com.cos.iter.web;

import com.cos.iter.util.Logging;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.cos.iter.config.auth.LoginUserAnnotation;
import com.cos.iter.config.auth.dto.LoginUser;
import com.cos.iter.service.ImageService;
import com.cos.iter.web.dto.ImageReqDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@Log4j2
public class ImageController {
	private final ImageService imageService;
	private final Logging logging;

	@GetMapping("/image/uploadForm")
	public String imageUploadForm(@RequestParam(name = "location", required = false) String location, Model model) {
		log.info(logging.getClassName() + " / " + logging.getMethodName());

		if(location == null) {
			log.info("Redirecting to location find page");
			return "redirect:/location/find";
		}

		model.addAttribute("location", location);
		return "image/image-upload";
	}
	
	@PostMapping("/image/upload")
	public String imageUpload(@LoginUserAnnotation LoginUser loginUser, ImageReqDto imageReqDto) {
		log.info(logging.getClassName() + " / " + logging.getMethodName());

		imageService.photoUploadToCloud(imageReqDto, loginUser.getId());
		return "redirect:/user/" + loginUser.getId();
	}
}




