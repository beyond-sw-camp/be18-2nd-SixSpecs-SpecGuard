package com.beyond.specguard.verification.repository;

import com.beyond.specguard.verification.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface PhoneVerificationRepo extends JpaRepository<PhoneVerification, String> {

    Optional<PhoneVerification> findByIdAndStatus(String id, String status);
    Optional<PhoneVerification> findByTokenAndStatus(String token, String status);

    boolean existsByToken(String token);

    @Query("""
        select count(v) from PhoneVerification v
         where v.phone = :phone
           and v.status = 'PENDING'
           and v.expiresAt > :now
    """)
    long countActiveByPhone(@Param("phone") String phone, @Param("now") Instant now);

    // [A] 해당 번호의 과거 PENDING 전부 만료
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PhoneVerification v
           set v.status = 'EXPIRED'
         where v.phone = :phone
           and v.status = 'PENDING'
    """)
    int expireAllPendingByPhone(@Param("phone") String phone);

    // [B] 현재 요청 id를 제외하고 과거 PENDING 만료
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PhoneVerification v
           set v.status = 'EXPIRED'
         where v.phone = :phone
           and v.status = 'PENDING'
           and v.id <> :currentId
    """)
    void expirePendingByPhoneExceptId(@Param("phone") String phone,
                                      @Param("currentId") String currentId);

    // PENDING & 미만료인 경우에만 상태 전이
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PhoneVerification v
           set v.status = :toStatus, v.usedAt = :usedAt
         where v.id = :id
           and v.status = 'PENDING'
           and v.expiresAt > :now
    """)
    int markIfPending(@Param("id") String id,
                      @Param("toStatus") String toStatus,
                      @Param("usedAt") Instant usedAt,
                      @Param("now") Instant now);

    // PENDING & 이미 만료된 건만 EXPIRED로
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update PhoneVerification v
           set v.status = 'EXPIRED'
         where v.id = :id
           and v.status = 'PENDING'
           and v.expiresAt <= :now
    """)
    int markExpiredIfPending(@Param("id") String id,
                             @Param("now") Instant now);

    // 만료된 레코드 청소
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from PhoneVerification v
         where v.expiresAt <= :now
           and v.status <> 'SUCCESS'
    """)
    int cleanup(@Param("now") Instant now);
}
