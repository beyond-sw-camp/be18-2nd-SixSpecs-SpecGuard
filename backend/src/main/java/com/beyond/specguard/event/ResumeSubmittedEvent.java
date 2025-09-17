package com.beyond.specguard.event;

import java.util.UUID;

public record ResumeSubmittedEvent(UUID resumeId, UUID templateId) {
}
