<<<<<<< HEAD:backend/src/main/java/com/beyond/specguard/auth/model/service/CustomUserDetailsService.java
package com.beyond.specguard.auth.model.service;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientUserRepository userRepository;

    public CustomUserDetailsService(ClientUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //  company 를 join fetch 해서 같이 로딩
        ClientUser user = userRepository.findByEmailWithCompany(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new CustomUserDetails(user);
    }
}
=======
package com.beyond.specguard.auth.model.service.local;

import com.beyond.specguard.auth.model.entity.ClientUser;
import com.beyond.specguard.auth.model.repository.ClientUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ClientUserRepository userRepository;

    public CustomUserDetailsService(ClientUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //  company 를 join fetch 해서 같이 로딩
        ClientUser user = userRepository.findByEmailWithCompany(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new CustomUserDetails(user);
    }
}
>>>>>>> develop:backend/src/main/java/com/beyond/specguard/auth/model/service/local/CustomUserDetailsService.java
