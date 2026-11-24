package org.example.resourcebookingappbackend.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        bearerFormat = "JWT",
        scheme = "Bearer",
        type = SecuritySchemeType.HTTP

)
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Resource Booking App API")
                        .version("1.0")
                        .description("""
                                Have wondered how to get the free space either restaurant ,offices , weeding gardens on time .
                                Space sync provides the ready-to consume API for reservation
                                
                                [Continue with Google](http://localhost:8081/oauth2/authorization/google)
                                """)
                        .contact(new Contact()
                                .url("https://spacesync.vercel.app")
                                .name("Space Sync."))
                        .license(new License()
                                .name("Apache 2.0")))
                .security(
                        List.of(new SecurityRequirement().addList("bearerAuth")));


    }
}
