package core.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StackHub Core API")
                        .description("결제 및 정산 시스템 API 문서")
                        .version("v1.0.0"))
                .tags(List.of(
                        new Tag().name("Payment").description("결제 API"),
                        new Tag().name("Member").description("회원 API")
                ));
    }
}
