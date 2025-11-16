package swyp.dodream.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT 토큰을 입력하세요"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(new Info()
                        .title("DoDream API")
                        .description("DoDream 백엔드 API 문서")
                        .version("v1.0"))
                .servers(Arrays.asList(
                        new Server().url("https://api.dodream.store").description("운영 서버"),
                        new Server().url("http://localhost:8080").description("로컬 서버")
                ));
    }
}
