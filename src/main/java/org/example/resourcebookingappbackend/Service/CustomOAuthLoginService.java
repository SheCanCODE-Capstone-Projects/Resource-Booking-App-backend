package org.example.resourcebookingappbackend.Service;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.example.resourcebookingappbackend.Enum.Role;
import org.example.resourcebookingappbackend.Model.Users;
import org.example.resourcebookingappbackend.Repository.UsersRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuthLoginService extends DefaultOAuth2UserService {

    private final UsersRepository usersRepository;

    public CustomOAuthLoginService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public @Nullable OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String email = oAuth2User.getAttribute("email");
        String fullName = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Email not found");
        }

        Users user = usersRepository.findUsersByUserName(email)
                .orElseGet(() -> {
                    Users newUser = new Users();
                    newUser.setUserName(email);
                    newUser.setFullName(fullName);
                    newUser.setAuthProvider(registrationId);
                    newUser.setPassword("");
                    newUser.setRole(Role.USER);

                    return usersRepository.save(newUser);
                });

        System.out.println("Loading Google user: " + email);
        return oAuth2User;
    }
}
