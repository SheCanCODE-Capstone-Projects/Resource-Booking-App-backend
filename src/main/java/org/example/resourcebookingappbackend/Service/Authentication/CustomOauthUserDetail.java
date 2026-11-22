package org.example.resourcebookingappbackend.Service.Authentication;

import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Model.Account;
import org.example.resourcebookingappbackend.Repository.AccountRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOauthUserDetail extends DefaultOAuth2UserService {
    private final AccountRepository accountRepository;

    public CustomOauthUserDetail(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @NullMarked
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        Map<String , Object> attributes =  oAuth2User.getAttributes();
        String username = (String) attributes.get("email");
        String fullName = (String) attributes.get("given_name");

        if (accountRepository.findAccountByUserName(username).isEmpty()){
            Account account = new Account();
            account.setUserName(username);
            account.setFullName(fullName);
            account.setPassword("");
            account.setAuthProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
            account.setRole(Role.USER);
            accountRepository.save(account);

        }


        return new DefaultOAuth2User(
                Collections.singleton(
                        new SimpleGrantedAuthority("ROLE_USER")
                ),
                attributes,
                "email"
        );





    }
}