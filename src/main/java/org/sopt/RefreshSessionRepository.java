package org.sopt;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RefreshSession s where s.id = :id")
    Optional<RefreshSession> findForUpdate(@Param("id") UUID id);

    boolean existsByIdAndMemberIdAndMemberDeletedFalseAndExpiresAtAfter(
            UUID id, long memberId, Instant now
    );

    @Modifying
    @Query("delete from RefreshSession s where s.member.id = :memberId")
    void deleteForMember(@Param("memberId") long memberId);

    @Modifying
    @Query("delete from RefreshSession s where s.expiresAt <= :now")
    void deleteExpired(@Param("now") Instant now);
}
