package org.example.communityservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.Exception.*;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.example.communityservice.dto.comment.response.CommentResponseDto;
import org.example.communityservice.dto.post.request.PostRequestDto;
import org.example.communityservice.dto.post.request.PostUpdateRequestDto;
import org.example.communityservice.dto.post.response.*;
import org.example.communityservice.entity.*;
import org.example.communityservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public List<PostListResponseDto> showPostList(Long userId){
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));

        List<PostListResponseDto> postListResponseDto = new ArrayList<>();
        List<Post> postList = postRepository.findAllByOrderByCreatedAtDesc();
        for (Post post : postList) {
            postListResponseDto.add(new PostListResponseDto(post));
        }
        return postListResponseDto;
    }

    @Transactional
    public PostCreateResponseDto createPost(Long userId, @Valid PostRequestDto postRequestDto){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));

        Post post = new Post(user, postRequestDto.getTitle(), postRequestDto.getContent(), 0, 0, 0);
        postRepository.save(post);

        int length = postRequestDto.getPostImage().size();
        if(length<=5) {
            for (int i = 0; i < length; i++) {
                PostImage postImage = new PostImage(post, postRequestDto.getPostImage().get(i), i+1);
                postImageRepository.save(postImage);
            }
        }
        else{
            throw new BadRequestException("not_exist", "newMaximum allowed images is 5");
        }

        List<PostImage> postImageList = postImageRepository.findByPost_PostIdOrderByImageOrderAsc(post.getPostId());
        List<PostImageResponseDto> postImageResponseDtoList = new ArrayList<>();
        for (PostImage postImage : postImageList) {
            postImageResponseDtoList.add(new PostImageResponseDto(postImage));
        }
        return new PostCreateResponseDto(post, postImageResponseDtoList);
    }

    @Transactional
    public PostDetailResponseDto showPostDetail(Long userId, Long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));
        post.increaseViewCount();
        System.out.println("after increase = " + post.getViewCount());

        List<PostImage> postImageList = postImageRepository.findByPost_PostIdOrderByImageOrderAsc(postId);
        if(postImageList.isEmpty()){
            throw new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post_image", "not_exist"))));
        }
        List<PostImageResponseDto> postImageResponseDtoList = new ArrayList<>();
        for (PostImage postImage : postImageList) {
            postImageResponseDtoList.add(new PostImageResponseDto(postImage));
        }

        List<Comment> commentList = commentRepository.findByPost_PostIdOrderByCreatedAtDesc(postId);
        if(commentList.isEmpty()){
            return new PostDetailResponseDto(post, postImageResponseDtoList, null);
        }

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            commentResponseDtoList.add(new CommentResponseDto(comment, user));
        }

        return new PostDetailResponseDto(post, postImageResponseDtoList, commentResponseDtoList);
    }

    @Transactional
    public PostUpdateResponseDto updatePost(Long userId, Long postId, @Valid PostUpdateRequestDto postUpdateRequestDto){
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException ("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));
        List<String> newPostImageList = postUpdateRequestDto.getPostImage();

        if(!userId.equals(post.getUser().getUserId())){
            throw new ForbiddenException();
        }
        if(postUpdateRequestDto.getTitle()==null && postUpdateRequestDto.getContent()==null && newPostImageList==null){
            throw new BadRequestException("invalid_request");
        }

        if(postUpdateRequestDto.getTitle()!=null){
            post.changeTitle(postUpdateRequestDto.getTitle());
        }
        if(postUpdateRequestDto.getContent()!=null){
            post.changeContent(postUpdateRequestDto.getContent());
        }
        if(newPostImageList!=null){
            if(newPostImageList.size()<=5) {
                List<PostImage> existPostImageList = postImageRepository.findByPost_PostId(postId);
                postImageRepository.deleteAll(existPostImageList);

                for (int i = 0; i < newPostImageList.size(); i++) {
                    postImageRepository.save(new PostImage(post, newPostImageList.get(i), i));
                }
            }
            else {
                throw new BadRequestException("not_exist", "Maximum allowed images is 5");
            }
        }
        post.changeUpdatedAt();
        List<PostImage> postImageList = postImageRepository.findByPost_PostIdOrderByImageOrderAsc(postId);
        List<PostImageResponseDto> postImageResponseDtoList = new ArrayList<>();
        for (PostImage postImage : postImageList) {
            postImageResponseDtoList.add(new PostImageResponseDto(postImage));
        }

        return new PostUpdateResponseDto(post.getTitle(), postImageResponseDtoList, post.getContent(), post.getUpdatedAt());
    }

    public void deletePost(Long userId, Long postId){
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        if(!userId.equals(post.getUser().getUserId())){
            throw new ForbiddenException();
        }
        postRepository.delete(post);
    }

    @Transactional
    public PostLikeCountResponseDto toggleLike(Long userId, Long postId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        PostLike postLike = postLikeRepository.findByPost_PostIdAndUser_UserId(postId, userId);
        if(post.getUser().getUserId().equals(userId)){
            throw new ForbiddenException();
        }
        if(!(postLike == null)) {
            postLikeRepository.delete(postLike);
            post.decreaseLikeCount();
        }
        else{
            postLikeRepository.save(new PostLike(user, post));
            post.increaseLikeCount();
        }
        return new PostLikeCountResponseDto(post.getLikeCount());
    }
}
