package com.beyond.specguard.resume.job;


import com.beyond.specguard.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeCleanupScheduler {

    private final ResumeService resumeService;

    @Value("${app.cleanup.resumes.enabled:true}")
    private boolean enabled;

    @Value("${app.cleanup.resume.chunk-size:100}")
    private int chunkSize;

    @Scheduled(cron = "${app.cleanup.resume.cron:0 10 3 * * *}", zone = "${app.timezone:Asia/Seoul}")
    public void runDailyCleanup() {
        if (!enabled) {
            log.debug("[cleanup] disabled by app.cleanup.resume.enabled=false");
            return;
        }
        int deleted = resumeService.cleanupExpiredUnsubmittedResumes(chunkSize);
        if (deleted > 0) {
            log.info("[cleanup] expired & unsubmitted resumes deleted: {}", deleted);
        } else {
            log.debug("[cleanup] nothing to delete this run");
        }
    }
}