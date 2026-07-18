package org.example.communityservice.repository;

import org.example.communityservice.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @EntityGraph(attributePaths = "user")
    Slice<Post> findAllByOrderByPostIdDesc(Pageable pageable);

    @EntityGraph(attributePaths = "user")
    Slice<Post> findByPostIdLessThanOrderByPostIdDesc(Long cursor, Pageable pageable);
}
