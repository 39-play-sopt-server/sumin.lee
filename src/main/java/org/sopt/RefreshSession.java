package org.sopt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

@Entity
class RefreshSession {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    protected RefreshSession() {
    }

    RefreshSession(Member member, String tokenHash, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.member = member;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    boolean accepts(String tokenHash, Instant now) {
        return expiresAt.isAfter(now)
                && MessageDigest.isEqual(this.tokenHash.getBytes(StandardCharsets.US_ASCII),
                        tokenHash.getBytes(StandardCharsets.US_ASCII))
                && !member.isDeleted();
    }

    void rotate(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    UUID id() {
        return id;
    }

    long memberId() {
        return member.getId();
    }

    Instant expiresAt() {
        return expiresAt;
    }
}
