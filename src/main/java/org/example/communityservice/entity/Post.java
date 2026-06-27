package org.example.communityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(nullable = false, length = 26)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "like_count", nullable = false)
    @Min(0)
    private int likeCount;

    @Column(name = "comment_count", nullable = false)
    @Min(0)
    private int commentCount;

    @Column(name = "view_count", nullable = false)
    @Min(0)
    private int viewCount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private User user;


    public Post(User user, String title, String content, int likeCount, int commentCount, int viewCount){
        this.user = user;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
    }


    public void increaseLikeCount(){
        this.likeCount+=1;
    }
    public void decreaseLikeCount(){
        this.likeCount-=1;
    }

    public void increaseCommentCount(){
        this.commentCount+=1;
    }
    public void decreaseCommentCount(){
        this.commentCount-=1;
    }

    public void increaseViewCount(){
        this.viewCount+=1;
    }

    public void changeTitle(String title){
        this.title = title;
    }

    public void changeContent(String content){
        this.content = content;
    }

    public void changeUpdatedAt(){
        this.updatedAt = LocalDateTime.now();
    }
}
