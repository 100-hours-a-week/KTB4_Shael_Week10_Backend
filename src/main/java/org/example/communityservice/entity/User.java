package org.example.communityservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, unique = true, length = 10)
    private String nickname;

    @Column(name = "profile_stored_filename", nullable = false, length = 500)
    private String profileStoredFilename;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(UserCreateRequestDto userCreateRequestDto, String profileStoredFilename){
        this.email = userCreateRequestDto.getEmail();
        this.password = userCreateRequestDto.getPassword();
        this.nickname = userCreateRequestDto.getNickname();

        if(profileStoredFilename==null){
            this.profileStoredFilename = "test_image.png";
        }
        else {
            this.profileStoredFilename = profileStoredFilename;
        }
        this.createdAt = LocalDateTime.now();
    }

    public void changeEmail(String email){
        this.email = email;
    }

    public void changePassword(String password){
        this.password = password;
    }

    public void changeNickname(String nickname){
        this.nickname = nickname;
    }

    public void changeProfileStoredFilename(String profileStoredFilename){
        this.profileStoredFilename = profileStoredFilename;
    }

    public void changeUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }
}
