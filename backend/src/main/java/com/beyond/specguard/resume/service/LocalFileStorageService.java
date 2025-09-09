package com.beyond.specguard.resume.service;

import com.beyond.specguard.common.exception.CustomException;
import com.beyond.specguard.resume.exception.errorcode.ResumeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalFileStorageService {

    @Value("${app.storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${app.storage.public-base-url:http://localhost:8080/files}")
    private String publicBaseUrl;

    public String saveProfileImage(UUID resumeId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ResumeErrorCode.INVALID_REQUEST);
        }
        String ct = Optional.ofNullable(file.getContentType()).orElse("");
        if (!ct.startsWith("image/")) {
            throw new CustomException(ResumeErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            Path dir = Paths.get(basePath, "profile", resumeId.toString());
            Files.createDirectories(dir);

            String ext = Optional.ofNullable(file.getOriginalFilename())
                    .filter(n -> n.contains("."))
                    .map(n -> n.substring(n.lastIndexOf('.')))
                    .orElse(".bin");

            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            String rel = String.join("/", "profile", resumeId.toString(), filename);
            return publicBaseUrl.replaceAll("/$", "") + "/" + rel;

        } catch (IOException e) {
            throw new CustomException(ResumeErrorCode.FILE_UPLOAD_ERROR);
        }
    }
}
