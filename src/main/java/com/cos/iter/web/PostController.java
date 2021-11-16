package com.cos.iter.web;

import com.cos.iter.config.auth.LoginUserAnnotation;
import com.cos.iter.config.auth.dto.LoginUser;
import com.cos.iter.domain.post.Post;
import com.cos.iter.service.PostService;
import com.cos.iter.util.Logging;
import com.cos.iter.util.Script;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
public class PostController {
    private final PostService postService;
    private final Logging logging;

    @Value("${azure.blob.url}")
    private String blobStorageUrl;

    @GetMapping({"", "/", "/post"})
    public String feed(String tag, @LoginUserAnnotation LoginUser loginUser,
                       @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        log.info(logging.getClassName() + " / " + logging.getMethodName());
        log.info("loginUser : " + loginUser);

        Page<Post> posts = postService.getFeedPhoto(loginUser.getId(), tag, page);
        log.info("Feed Posts: " + posts);

        model.addAttribute("posts", posts);
        model.addAttribute("storageUrl", blobStorageUrl);
        model.addAttribute("prevPage", posts.getNumber());
        model.addAttribute("nextPage", posts.getNumber() + 2);

        return "image/feed";
    }

    @GetMapping("/post/feed/test")
    public @ResponseBody
    Page<Post> testFeed(String tag, @LoginUserAnnotation LoginUser loginUser,
                        @RequestParam(name = "page", defaultValue = "1") int page) {
        log.info(logging.getClassName() + " / " + logging.getMethodName());

        return postService.getFeedPhoto(loginUser.getId(), tag, page);
    }

    @GetMapping("/post/uploadForm")
    public String imageUploadForm(@RequestParam(name = "location", required = false) String location, Model model) {
        log.info(logging.getClassName() + " / " + logging.getMethodName());

        if(location == null) {
            log.info("Redirecting to location find page");
            return "redirect:/location/find";
        }

        model.addAttribute("location", location);
        model.addAttribute("storageUrl", blobStorageUrl);
        return "image/image-upload";
    }

    @GetMapping("/post/explore")
    public String imageExplore(@LoginUserAnnotation LoginUser loginUser,
                               @RequestParam(name = "page", defaultValue = "1") int page, Model model) {
        log.info(logging.getClassName() + " / " + logging.getMethodName());
        log.info("loginUser: " + loginUser);

        List<Post> posts = postService.getPopularPost(loginUser.getId(), page);
        model.addAttribute("posts", posts);
        model.addAttribute("storageUrl", blobStorageUrl);

        Page<Post> postsWithPaging = postService.getPopularPostWithPage(loginUser.getId(), page);

        model.addAttribute("prevPage", postsWithPaging.getNumber());
        model.addAttribute("nextPage", postsWithPaging.getNumber() + 2);
        model.addAttribute("isFirst", postsWithPaging.isFirst());
        model.addAttribute("isLast", postsWithPaging.isLast());

        return "image/explore";
    }

    @GetMapping(path = "/post/detail/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String detailView(@LoginUserAnnotation LoginUser loginUser, @PathVariable(name="postId") int postId,
                             HttpServletResponse response, Model model) throws IOException {
        log.info(logging.getClassName() + " / " + logging.getMethodName());
        log.info("loginUser : " + loginUser);

        Post post = postService.getDetailPost(loginUser.getId(), postId);
        log.info("Got Post: " + post);

        if(post == null || !post.getVisible()) {
            response.setContentType("text/html; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(Script.back("이 글은 볼 수 없는 글입니다."));
            out.flush();

            return null;
        }

        model.addAttribute("posts", post);
        model.addAttribute("storageUrl", blobStorageUrl);
        model.addAttribute("mapData", post.toJavaScriptData());

        postService.increaseViewCount(post);
        return "post/detail";
    }

    @DeleteMapping("/post/{id}")
    public ResponseEntity<?> postDelete(@PathVariable int id) {
        log.info(logging.getClassName() + " / " + logging.getMethodName());

        postService.setPostNotVisible(id);
        return new ResponseEntity<String>("ok", HttpStatus.OK);
    }
}
