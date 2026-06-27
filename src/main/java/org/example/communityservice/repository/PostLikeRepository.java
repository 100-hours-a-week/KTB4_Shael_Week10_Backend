package org.example.communityservice.repository;

import org.example.communityservice.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    PostLike findByPost_PostIdAndUser_UserId(Long postId, Long userId);
}
