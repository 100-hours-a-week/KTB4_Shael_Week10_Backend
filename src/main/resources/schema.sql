CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       nickname VARCHAR(10) NOT NULL UNIQUE,
                       profile_image VARCHAR(500) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NULL,
                       PRIMARY KEY (user_id)
);

CREATE TABLE posts (
                       post_id BIGINT AUTO_INCREMENT,
                       writer_id BIGINT NOT NULL,
                       title VARCHAR(26) NOT NULL,
                       content TEXT NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NULL,
                       like_count INT NOT NULL DEFAULT 0 CHECK (like_count >= 0),
                       comment_count INT NOT NULL DEFAULT 0 CHECK (comment_count >= 0),
                       view_count INT NOT NULL DEFAULT 0 CHECK (view_count >= 0),
                       PRIMARY KEY (post_id),
                       FOREIGN KEY (writer_id) REFERENCES users(user_id)
);

CREATE TABLE post_images (
                             post_images_id BIGINT AUTO_INCREMENT,
                             post_id BIGINT NOT NULL,
                             post_image VARCHAR(500) NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP NULL,
                             PRIMARY KEY (post_images_id),
                             FOREIGN KEY (post_id) REFERENCES posts(post_id)
);

CREATE TABLE comments (
                          comment_id BIGINT AUTO_INCREMENT,
                          post_id BIGINT NOT NULL,
                          parent_comment_id BIGINT NULL,
                          writer_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NULL,
                          PRIMARY KEY (comment_id),
                          FOREIGN KEY (post_id) REFERENCES posts(post_id),
                          FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id),
                          FOREIGN KEY (writer_id) REFERENCES users(user_id)
);

CREATE TABLE post_likes (
                            post_likes_id BIGINT AUTO_INCREMENT,
                            user_id BIGINT NOT NULL,
                            post_id BIGINT NOT NULL,
                            created_at TIMESTAMP NOT NULL,
                            PRIMARY KEY (post_likes_id),
                            UNIQUE (user_id, post_id),
                            FOREIGN KEY (user_id) REFERENCES users(user_id),
                            FOREIGN KEY (post_id) REFERENCES posts(post_id)
);