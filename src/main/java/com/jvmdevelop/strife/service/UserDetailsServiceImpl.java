package com.jvmdevelop.strife.service;

import com.jvmdevelop.strife.model.UserDetailsImpl;
import com.jvmdevelop.strife.repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserDetailsImpl.build(
                userRepo.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
        );
    }
}
