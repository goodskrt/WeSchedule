package com.iusjc.weschedule.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI wescheduleOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WeSchedule API")
                        .description("API pour le système de gestion d'emploi du temps WeSchedule")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("IUSJC Development Team")
                                .email("contact@iusjc.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Serveur de développement")
                ));
    }
}