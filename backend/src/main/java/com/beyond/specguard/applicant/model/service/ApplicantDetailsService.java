package com.beyond.specguard.applicant.model.service;

import com.beyond.specguard.tempresume.Resume;
import com.beyond.specguard.tempresume.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicantDetailsService implements UserDetailsService {

    private final ResumeRepository resumeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Resume resume = resumeRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new ApplicantDetails(resume);
    }
}
