package edu.eci.arsw.blueprints.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("ARSW Blueprints API")
                        .version("v1")
                        .description("REST API para gestión de blueprints — Lab #4 Arquitecturas de Software")
                        .contact(new Contact()
                                .name("Escuela Colombiana de Ingeniería")
                                .url("https://www.escuelaing.edu.co"))
                        .license(new License()
                                .name("CC BY-NC 4.0")
                                .url("https://creativecommons.org/licenses/by-nc/4.0/")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local")));
    }
}