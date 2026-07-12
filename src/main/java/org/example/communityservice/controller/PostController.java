package org.example.communityservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.dto.post.request.PostRequestDto;
import org.example.communityservice.dto.post.request.PostUpdateRequestDto;
import org.example.communityservice.dto.post.response.*;
import org.example.communityservice.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{userId}/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<CommonResponseDto<List<PostListResponseDto>>> showPostList(@PathVariable Long userId){
        List<PostListResponseDto> postListResponseDto = postService.showPostList(userId);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", postListResponseDto));
    }

    @PostMapping
    public ResponseEntity<CommonResponseDto<PostResponseDto>> createPost(@PathVariable Long userId, @Valid @RequestBody PostRequestDto postRequestDto){
        PostResponseDto postResponseDto = postService.createPost(userId, postRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponseDto<>("register_success", postResponseDto));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<CommonResponseDto<PostDetailResponseDto>> showPostDetail(@PathVariable Long userId, @PathVariable Long postId){
        PostDetailResponseDto postDetailResponseDto = postService.showPostDetail(userId, postId);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", postDetailResponseDto));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<CommonResponseDto<PostResponseDto>> updatePost(@PathVariable Long userId, @PathVariable Long postId, @Valid @RequestBody PostUpdateRequestDto postUpdateRequestDto){
        PostResponseDto postUpdateResponseDto = postService.updatePost(userId, postId, postUpdateRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", postUpdateResponseDto));
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
