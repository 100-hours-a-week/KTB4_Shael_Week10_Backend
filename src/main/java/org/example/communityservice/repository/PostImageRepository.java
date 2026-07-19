package org.example.communityservice.repository;

import org.example.communityservice.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    List<PostImage> findByPost_PostId(Long postId);
    List<PostImage> findByPost_PostIdOrderByImageOrderAsc(Long postId);

    @Query("""
        select pi.storedFilename
        from PostImage pi
        where pi.post.postId = :postId
   """)
    List<String> findStoredFilenamesByPostId(
            @Param("postId") Long postId
    );
}
