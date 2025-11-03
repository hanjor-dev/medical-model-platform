/**
 * SpringDoc OpenAPI配置类：API文档配置
 * 使用SpringDoc替代Springfox，适配Spring Boot 3
 * 
 * @author hanjor
 * @version 3.0
 * @date 2025-10-31
 */
package com.okbug.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(name = "springdoc.api-docs.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    /**
     * 自定义OpenAPI配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("医学影像模型管理平台API")
                        .description("医学影像模型管理平台接口文档 - Spring Boot 3.3.5 + JDK 21")
                        .version("3.0.0")
                        .contact(new Contact()
                                .name("hanjor")
                                .email("hanjor@qq.com")
                                .url("https://github.com/yourusername/medical-model-platform"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("/api")
                                .description("当前服务器")
                ));
    }
}

