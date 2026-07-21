package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.common.util.UserDisplayUtils;
import org.example.communityservice.dto.comment.response.CommentResponseDto;
import org.example.communityservice.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonPropertyOrder({
        "title",
        "writerId",
        "writerNickname",
        "writerProfileImage",
        "isOwner",
        "postImages",
        "content",
        "createdAt",
        "updatedAt",
        "likeCount",
        "isLiked",
        "commentCount",
        "viewCount",
        "commentList"
})
public class PostDetailResponseDto {
    private String title;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private boolean isOwner;
    private List<PostImageResponseDto> postImages;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private int likeCount;
    private boolean isLiked;
    private int commentCount;
    private int viewCount;
    private List<CommentResponseDto> commentList;


    public PostDetailResponseDto(Post post, boolean isOwner,  List<PostImageResponseDto> postImageResponseDto, boolean isLiked, List<CommentResponseDto> commentList){
        this.title = post.getTitle();
        this.writerId = post.getUser().getUserId();
        this.writerNickname = UserDisplayUtils.nickname(post.getUser());
        this.writerProfileImage = UserDisplayUtils.profileStoredFilename(post.getUser());
        this.isOwner = isOwner;
        this.postImages = postImageResponseDto;
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikeCount();
        this.isLiked = isLiked;
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.commentList = commentList;
    }
}
