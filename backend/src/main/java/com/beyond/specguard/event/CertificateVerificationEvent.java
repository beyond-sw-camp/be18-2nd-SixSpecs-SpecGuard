package com.beyond.specguard.event;

import java.util.UUID;

public record CertificateVerificationEvent(UUID resumeId) { }