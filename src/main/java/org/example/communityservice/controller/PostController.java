package org.example.communityservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.dto.post.request.PostRequestDto;
import org.example.communityservice.dto.post.request.PostUpdateRequestDto;
import org.example.communityservice.dto.post.response.*;
import org.example.communityservice.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{userId}/posts")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<CommonResponseDto<PostListCursorResponseDto>> showPostList(@PathVariable Long userId, @RequestParam(required = false) Long cursor){
        PostListCursorResponseDto postListCursorResponseDto = postService.showPostList(userId, cursor);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", postListCursorResponseDto));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<PostResponseDto>> createPost(@PathVariable Long userId, @Valid @RequestPart("content") PostRequestDto postRequestDto, @RequestPart("images") List<MultipartFile> images){
        PostResponseDto postResponseDto = postService.createPost(userId, postRequestDto, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponseDto<>("register_success", postResponseDto));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponseDto<PostDetailResponseDto>> showPostDetail(@PathVariable Long userId, @PathVariable Long postId){
        PostDetailResponseDto postDetailResponseDto = postService.showPostDetail(userId, postId);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", postDetailResponseDto));
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<PostResponseDto>> updatePost(@PathVariable Long userId, @PathVariable Long postId, @Valid @RequestPart("content") PostUpdateRequestDto postUpdateRequestDto, @RequestPart(value = "images", required = false)
                                                                         List<MultipartFile> images){
        PostResponseDto postResponseDto = postService.updatePost(userId, postId, postUpdateRequestDto, images);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", postResponseDto));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<CommonResponseDto<Void>> deletePost(@PathVariable Long userId, @PathVariable Long postId){
        postService.deletePost(userId, postId);

        return ResponseEntity.ok(new CommonResponseDto<>("delete_success", null));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<CommonResponseDto<PostLikeCountResponseDto>> toggleLike(@PathVariable Long userId, @PathVariable Long postId){
        PostLikeCountResponseDto postLikeCountResponseDto = postService.toggleLike(userId, postId);

        return ResponseEntity.ok(new CommonResponseDto<>("register_success", postLikeCountResponseDto));
    }
}
