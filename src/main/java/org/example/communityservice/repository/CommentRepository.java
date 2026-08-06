package org.example.communityservice.repository;

import org.example.communityservice.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "user")
    List<Comment> findByPost_PostIdOrderByCreatedAtAsc(Long postId);

    Optional<Comment> findByCommentIdAndDeletedAtIsNull(Long commentId);
    void deleteAllByPost_PostId(Long postId);

    @Query("""
        select c
        from Comment c
        where c.post.id = :postId
          and (
                c.deletedAt is null
                or exists (
                    select 1
                    from Comment child
                    where child.parentComment = c
                      and child.deletedAt is null
                )
          )
        order by c.createdAt asc
    """)
    List<Comment> findVisibleCommentsByPostId(
            @Param("postId") Long postId
    );
}
