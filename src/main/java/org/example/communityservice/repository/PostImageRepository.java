package org.example.communityservice.repository;

import org.example.communityservice.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPost_PostId(Long postId);
    List<PostImage> findByPost_PostIdOrderByImageOrderAsc(Long postId);
}
