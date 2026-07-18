package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({
        "posts",
        "nextCursor",
        "hasNext"
})
public class PostListCursorResponseDto {
    private List<PostListResponseDto> posts;
    private Long nextCursor;
    private boolean hasNext;
}
