package swyp.dodream.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("http://49.50.132.63:8080").description("운영 서버")
                ));
    }

    private Info apiInfo() {
        return new Info()
                .title("DoDream API")
                .description("DoDream Backend API")
                .version("v0.1.0");
    }
}

