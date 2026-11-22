package org.example.resourcebookingappbackend.Service;

import org.example.resourcebookingappbackend.Model.Users;
import org.example.resourcebookingappbackend.Repository.UsersRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final UsersRepository usersRepository;

    public CustomUserDetailService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users user =  usersRepository
                .findUsersByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found: " + username));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_"+ user.getRole().name());

        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUserName())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .authorities(authority)
                .build()
                ;
    }
}
