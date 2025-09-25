package com.beyond.specguard.crawling.repository;

import com.beyond.specguard.crawling.entity.GitHubMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface GithubMetadataRepository extends JpaRepository<GitHubMetadata, UUID> {
    @Query("select s from GitHubMetadata s where s.resumeLink.id = :resumeLinkId")
    Optional<GitHubMetadata> findByResumeLinkId(UUID resumeLinkId);

    @Query("""
    select gm
    from GitHubMetadata gm
    join gm.resumeLink rl
    join rl.resume r
    where r.id = :resumeId
""")
    Optional<GitHubMetadata> findByResumeId(UUID resumeId);
}