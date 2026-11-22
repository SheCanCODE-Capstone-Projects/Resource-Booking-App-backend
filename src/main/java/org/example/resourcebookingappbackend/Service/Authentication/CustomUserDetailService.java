package org.example.resourcebookingappbackend.Service.Authentication;

import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final AccountRepository AccountRepository;

    public CustomUserDetailService(AccountRepository AccountRepository) {
        this.AccountRepository = AccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

       Account account =  AccountRepository
                .findAccountByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found: " + username));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+ account.getRole().name());

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(account.getUserName())
                .password(account.getPassword())
                .roles(account.getRole().name())
                .authorities(authority)
                .build()
                ;
    }
}
