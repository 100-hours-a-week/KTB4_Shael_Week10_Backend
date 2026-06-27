package org.example.communityservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_images", uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_images_post_order", columnNames = {"post_id", "image_order"})}
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id")
    private Long postImageId;

    @Column(name = "post_image", nullable = false, length = 500)
    private String postImage;

    @Column(name = "image_order", nullable = false)
    @Min(1)
    private int imageOrder;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private Post post;


    public PostImage(Post post, String postImage, int imageOrder){
        this.post = post;
        this.postImage = postImage;
        this.imageOrder = imageOrder;
    }
}
