package com.example.demo.services;

import com.example.demo.entities.Credential;
import com.example.demo.repositories.CredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.dtos.RegisterRequestDTO;

import java.util.Collections;
import java.util.List;

@Service
public class CredentialDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Credential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(credential.getRole())
        );

        return new User(credential.getUsername(), credential.getPassword(), authorities);
    }

    public void register(RegisterRequestDTO registerRequestDTO) {
        if (credentialRepository.existsByUsername(registerRequestDTO.username())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        Credential credential = new Credential(
                registerRequestDTO.username(),
                passwordEncoder.encode(registerRequestDTO.password()),
                registerRequestDTO.role()
        );

        credentialRepository.save(credential);
    }

    @Transactional
    public void deleteByUsername(String username) {
        Credential credential = credentialRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        credentialRepository.delete(credential);
    }
}
