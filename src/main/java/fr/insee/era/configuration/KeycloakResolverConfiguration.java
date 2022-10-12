package fr.insee.era.configuration;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class KeycloakResolverConfiguration {
    /**
     * Le resolver est mis dans une classe différente pour éviter les dépendances circulaires
     * @param env
     * @return
     */
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver(Environment env) {
        return new KeycloakSpringBootConfigResolver();
    }
}
