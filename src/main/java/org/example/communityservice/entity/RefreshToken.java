package org.example.communityservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public RefreshToken(String token, Long userId, LocalDateTime expiresAt){
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
    public boolean isExpired(){
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
}
