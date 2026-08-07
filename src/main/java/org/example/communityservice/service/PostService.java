package org.example.communityservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.exception.*;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.example.communityservice.dto.comment.response.CommentResponseDto;
import org.example.communityservice.dto.post.request.PostRequestDto;
import org.example.communityservice.dto.post.request.PostUpdateRequestDto;
import org.example.communityservice.dto.post.response.*;
import org.example.communityservice.entity.*;
import org.example.communityservice.repository.*;
import org.example.communityservice.storage.PostImageStorage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostImageStorage postImageStorage;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional(readOnly = true)
    public PostListCursorResponseDto showPostList(Long userId, Long cursor){
        userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));

        Pageable pageable = PageRequest.of(0, 10);
        Slice<Post> postSlice;

        if(cursor == null){
            postSlice = postRepository.findAllByOrderByPostIdDesc(pageable);
        }
        else{
            postSlice = postRepository.findByPostIdLessThanOrderByPostIdDesc(cursor, pageable);
        }

        List<PostListResponseDto> posts = new ArrayList<>();

        for(Post post : postSlice.getContent()){
            PostListResponseDto postListResponseDto = new PostListResponseDto(post);
            posts.add(postListResponseDto);
        }

        Long nextCursor;
        if(!(posts.isEmpty())){
            nextCursor = posts.getLast().getPostId();
        }
        else {
            nextCursor = null;
        }

        return new PostListCursorResponseDto(posts, nextCursor, postSlice.hasNext());
    }

    @Transactional
    public PostResponseDto createPost(Long userId, @Valid PostRequestDto postRequestDto, List<MultipartFile> images){
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        validateImages(images);

        Post post = new Post(user, postRequestDto.getTitle(), postRequestDto.getContent(), 0, 0, 0);
        postRepository.save(post);

        List<PostImage> postImageList = new ArrayList<>();

        for(int i = 0; i < images.size(); i++){
            MultipartFile image = images.get(i);

            String storedFilename = postImageStorage.store(image);
            PostImage postImage = new PostImage(post, storedFilename, i+1);

            postImageList.add(postImage);
        }
        postImageRepository.saveAll(postImageList);
        return new PostResponseDto(post.getPostId());
    }

    private void validateImages(List<MultipartFile> images){
        if(images == null || images.isEmpty()){
            throw new BadRequestException("invalid_request", "이미지를 업로드해주세요.");
        }
        if(images.size() > 5){
            throw new BadRequestException("invalid_request", "이미지는 최대 5장까지 업로드할 수 있습니다.");
        }
        if(images.stream().anyMatch(image -> image.isEmpty())){
            throw new BadRequestException("invalid_request", "빈 이미지 파일은 업로드할 수 없습니다.");
        }
    }

    @Transactional
    public PostDetailResponseDto showPostDetail(Long userId, Long postId){
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));
        post.increaseViewCount();

        List<PostImage> postImageList = postImageRepository.findByPost_PostIdOrderByImageOrderAsc(postId);
        if(postImageList.isEmpty()){
            throw new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post_image", "not_exist"))));
        }
        List<PostImageResponseDto> postImageResponseDtoList = new ArrayList<>();
        for (PostImage postImage : postImageList) {
            postImageResponseDtoList.add(new PostImageResponseDto(postImage));
        }

        boolean isOwner = false;
        boolean isLiked = false;
        if(userId.equals(post.getUser().getUserId())){
            isOwner = true;
        }
        PostLike postLike = postLikeRepository.findByPost_PostIdAndUser_UserId(postId, userId);
        if(postLike!=null){
            isLiked = true;
        }

        List<Comment> commentList = commentRepository.findVisibleCommentsByPostId(postId);
        if(commentList.isEmpty()){
            return new PostDetailResponseDto(post, isOwner, postImageResponseDtoList, isLiked, null);
        }

        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        for (Comment comment : commentList) {
            commentResponseDtoList.add(new CommentResponseDto(comment, user));
        }

        return new PostDetailResponseDto(post, isOwner, postImageResponseDtoList, isLiked, commentResponseDtoList);
    }

    @Transactional
    public PostResponseDto updatePost(Long userId, Long postId, @Valid PostUpdateRequestDto postUpdateRequestDto, List<MultipartFile> images){
        userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException ("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        if(!userId.equals(post.getUser().getUserId())){
            throw new ForbiddenException();
        }
        if(postUpdateRequestDto.getTitle()==null && postUpdateRequestDto.getContent()==null && images==null){
            throw new BadRequestException("invalid_request");
        }

        if(postUpdateRequestDto.getTitle()!=null){
            post.changeTitle(postUpdateRequestDto.getTitle());
        }
        if(postUpdateRequestDto.getContent()!=null){
            post.changeContent(postUpdateRequestDto.getContent());
        }
        if(images!=null){
            replacePostImages(post, images);
        }
        post.changeUpdatedAt();

        return new PostResponseDto(postId);
    }

    private void replacePostImages(Post post, List<MultipartFile> images){
        validateImages(images);

        List<PostImage> existingImages = postImageRepository.findByPost_PostId(post.getPostId());
        List<String> newStoredFilenames = new ArrayList<>();
        List<PostImage> newPostImages = new ArrayList<>();

        try{
            for(int i=0; i< images.size(); i++){
                MultipartFile image = images.get(i);

                String storedFilename = postImageStorage.store(image);
                newStoredFilenames.add(storedFilename);

                newPostImages.add(new PostImage(post, storedFilename, i+1));
            }
            postImageRepository.deleteAll(existingImages);
            postImageRepository.flush();

            postImageRepository.saveAll(newPostImages);
        }
        catch (RuntimeException e){
            for(String storedFilename : newStoredFilenames){
                postImageStorage.delete((storedFilename));
            }
            throw new FileStorageException();
        }

        for(PostImage existingImage : existingImages){
            postImageStorage.delete(existingImage.getStoredFilename());
        }
    }

    @Transactional
    public void deletePost(Long userId, Long postId){
        userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        if(!userId.equals(post.getUser().getUserId())){
            throw new ForbiddenException();
        }

        List<String> storedFilenames = postImageRepository.findStoredFilenamesByPostId(postId);
        postLikeRepository.deleteAllByPost_PostId(postId);
        commentRepository.deleteAllByPost_PostId(postId);
        postImageRepository.deleteAllByPost_PostId(postId);

        postRepository.delete(post);
        postRepository.flush();

        for(String storedFilename : storedFilenames){
           postImageStorage.delete(storedFilename);
        }
    }

    @Transactional
    public PostLikeCountResponseDto toggleLike(Long userId, Long postId){
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        boolean isLiked;
        PostLike postLike = postLikeRepository.findByPost_PostIdAndUser_UserId(postId, userId);
        if(post.getUser().getUserId().equals(userId)){
            throw new ForbiddenException();
        }
        if(!(postLike == null)) {
            postLikeRepository.delete(postLike);
            isLiked = false;
            post.decreaseLikeCount();
        }
        else{
            postLikeRepository.save(new PostLike(user, post));
            isLiked = true;
            post.increaseLikeCount();
        }
        return new PostLikeCountResponseDto(isLiked, post.getLikeCount());
    }
}
