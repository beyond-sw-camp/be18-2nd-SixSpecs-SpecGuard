package com.beyond.specguard.admin.model.service;

import com.beyond.specguard.admin.model.entity.InternalAdmin;
import com.beyond.specguard.admin.model.repository.InternalAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InternalAdminDetailService implements UserDetailsService {

    private final InternalAdminRepository internalAdminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        InternalAdmin admin = internalAdminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new InternalAdminDetails(admin);
    }
}
