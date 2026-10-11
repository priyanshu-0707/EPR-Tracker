package com.eprtracker.security;

import com.eprtracker.entity.Company;
import com.eprtracker.repository.CompanyRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CompanyRepository companyRepository;

    public UserDetailsServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Company not found with email: " + email));

        return new User(
                company.getEmail(),
                company.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_COMPANY"))
        );
    }

    public Company loadCompanyById(String companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new UsernameNotFoundException("Company not found with id: " + companyId));
    }
}
