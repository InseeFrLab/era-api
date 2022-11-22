package fr.insee.era.configuration;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

        //Administrateur authorisé pour les actions sur le domaine RP
        @Value("${fr.insee.era.admin.role.rp:#{null}}") private String adminRP;


    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        //Actuator (health check)
        "/actuator/**",
    };

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                super.configure(http);
                http.cors(withDefaults()).authorizeRequests()
                    .antMatchers(SWAGGER_WHITELIST).permitAll()
                    .antMatchers("/extraction-survey-unit/**").hasRole(adminRP);
                // Zone open bar
                http.authorizeRequests().and().formLogin().permitAll().and().logout().permitAll();
                http.csrf().disable();
        }


        @Autowired public void configureGlobal(AuthenticationManagerBuilder auth) {
                KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
                keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
                auth.authenticationProvider(keycloakAuthenticationProvider);
        }

        @Bean @Override protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
                return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
        }

}

