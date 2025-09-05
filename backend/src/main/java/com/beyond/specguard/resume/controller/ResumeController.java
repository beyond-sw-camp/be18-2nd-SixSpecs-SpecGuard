package com.beyond.specguard.resume.controller;

import com.beyond.specguard.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {
    private final ResumeService resumeService;


}