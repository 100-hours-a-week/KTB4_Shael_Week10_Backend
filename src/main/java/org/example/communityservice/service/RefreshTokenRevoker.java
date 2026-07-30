package org.example.communityservice.service;

import lombok.RequiredArgsConstructor;
import org.example.communityservice.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenRevoker {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeHash(String refreshTokenHash) {
        refreshTokenRepository.deleteByToken(refreshTokenHash);
    }
}
