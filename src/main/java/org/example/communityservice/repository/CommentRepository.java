package org.example.communityservice.repository;

import org.example.communityservice.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost_PostIdOrderByCreatedAtDesc(Long postId);

    Optional<Comment> findByCommentIdAndDeletedAtIsNull(Long commentId);
    void deleteAllByPost_PostId(Long postId);
}
