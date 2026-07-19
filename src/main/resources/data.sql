INSERT INTO users (
    user_id,
    email,
    password,
    nickname,
    profile_stored_filename,
    created_at,
    updated_at
) VALUES
      (1, 'minji@example.com', 'Minji1234!', '민지', 'test_image.png', '2026-06-01 09:00:00', NULL),
      (2, 'junho@example.com', 'Junho1234@', '준호', 'test_image.png', '2026-06-01 09:15:00', NULL),
      (3, 'seoyeon@example.com', 'Seoyeon1234#', '서연', 'test_image.png', '2026-06-01 09:30:00', NULL),
      (4, 'hyunwoo@example.com', 'Hyunwoo1234$', '현우', 'test_image.png', '2026-06-01 09:45:00', NULL);

INSERT INTO posts (
    post_id,
    writer_id,
    title,
    content,
    created_at,
    updated_at,
    like_count,
    comment_count,
    view_count
) VALUES
      (1, 1, '오늘의 기록', '오늘은 날씨가 좋아서 산책을 다녀왔습니다.', '2026-06-02 10:00:00', NULL, 2, 3, 18),
      (2, 2, 'Spring 공부 중', 'JPA 연관관계 매핑을 공부하고 있습니다.', '2026-06-02 11:20:00', NULL, 2, 2, 25),
      (3, 3, '점심 추천', '오늘 점심 메뉴로 김치찌개를 추천합니다.', '2026-06-02 12:10:00', NULL, 1, 1, 9),
      (4, 4, '프로젝트 회고', '기능 구현보다 설계가 더 어렵다는 걸 느꼈습니다.', '2026-06-02 14:30:00', NULL, 0, 0, 4);

INSERT INTO post_images (
    post_image_id,
    post_id,
    original_filename,
    stored_filename,
    image_order
) VALUES
      (1, 1, 'test_image.png','test-image-1.png', 1),
      (2, 1, 'test_image.png','test-image-2.png',  2),
      (3, 2, 'test_image.png','test-image-3.png',  1),
      (4, 3, 'test_image.png','test-image-4.png',  1),
      (5, 4, 'test_image.png','test-image-5.png',  1);

INSERT INTO comments (
    comment_id,
    post_id,
    parent_comment_id,
    writer_id,
    content,
    created_at,
    updated_at
) VALUES
      (1, 1, NULL, 2, '산책 코스 좋아 보이네요.', '2026-06-02 10:10:00', NULL),
      (2, 1, 1, 1, '근처 공원인데 조용해서 좋았어요.', '2026-06-02 10:15:00', NULL),
      (3, 1, 2, 3, '저도 다음에 가봐야겠네요.', '2026-06-02 10:20:00', NULL),

      (4, 2, NULL, 3, '저도 JPA 공부 중인데 어렵더라고요.', '2026-06-02 11:30:00', NULL),
      (5, 2, 4, 2, '맞아요. 특히 연관관계가 헷갈려요.', '2026-06-02 11:35:00', NULL),

      (6, 3, NULL, 4, '김치찌개 좋네요. 저녁으로 먹어야겠어요.', '2026-06-02 12:20:00', NULL);

INSERT INTO post_likes (
    post_like_id,
    user_id,
    post_id,
    created_at
) VALUES
      (1, 2, 1, '2026-06-02 10:12:00'),
      (2, 3, 1, '2026-06-02 10:22:00'),
      (3, 1, 2, '2026-06-02 11:40:00'),
      (4, 3, 2, '2026-06-02 11:45:00'),
      (5, 4, 3, '2026-06-02 12:25:00');

ALTER TABLE users ALTER COLUMN user_id RESTART WITH 5;
ALTER TABLE posts ALTER COLUMN post_id RESTART WITH 5;
ALTER TABLE post_images ALTER COLUMN post_image_id RESTART WITH 6;
ALTER TABLE comments ALTER COLUMN comment_id RESTART WITH 7;
ALTER TABLE post_likes ALTER COLUMN post_like_id RESTART WITH 6;