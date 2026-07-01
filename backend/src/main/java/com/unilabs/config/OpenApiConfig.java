package com.unilabs.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationCenterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Unilabs Notification Center API")
                        .description("""
                                API REST para submissão, consulta e filtragem de notificações multicanal (EMAIL, SMS, PUSH).
                                As notificações são processadas de forma assíncrona via RabbitMQ.
                                
                                **Autenticação:** inclua o header `X-API-Key` em todos os pedidos.
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Unilabs").email("suporte@unilabs.pt")))
                .servers(List.of(new Server().url("http://localhost:8080").description("Ambiente local")))
                .addSecurityItem(new SecurityRequirement().addList("X-API-Key"))
                .components(new Components()
                        .addSecuritySchemes("X-API-Key", new SecurityScheme()
                                .name("X-API-Key")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)));
    }
}
