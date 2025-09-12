package com.beyond.specguard.client.model.service.local;

import com.beyond.specguard.client.model.entity.ClientUser;
import com.beyond.specguard.client.model.repository.ClientUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClientUserDetailsService implements UserDetailsService {

    private final ClientUserRepository userRepository;

    public ClientUserDetailsService(ClientUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //  company 를 join fetch 해서 같이 로딩
        ClientUser user = userRepository.findByEmailWithCompany(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new ClientUserDetails(user);
    }
}
