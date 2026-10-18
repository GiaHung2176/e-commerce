package com.giahung19.ecommerce_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    
    @Bean
    public OpenAPI ecommerceOpenApi() {
        return new OpenAPI()
            .info(new Info()
                    .title("E-commerce API")
                    .description("REST API quản lý sản phẩm và danh mục cho hệ thống bán hàng")
                    .version("0.0.1"))
            .components(new Components()
                    .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
