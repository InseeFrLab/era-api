package fr.insee.era.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

        //Administrateur authorisé pour les actions sur le domaine RP
        @Value("${fr.insee.era.admin.role.rp:#{null}}") private String adminRP;

        private String ROLE_PREFIX = "ROLE_";

        @Value("${fr.insee.era.security.oidc-claim-username:sub}")
        private String oidcClaimUsername;

        /** Path to the role field in token. For instance realm_access.role */
        @Value("${fr.insee.era.security.oidc-claim-role:realm_access.roles}")
        private String oidcClaimRole;

    private static final String[] SWAGGER_WHITELIST = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        //Actuator (health check)
        "/actuator/**",
    };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                /* http.cors(withDefaults()).authorizeRequests()
                    .antMatchers(SWAGGER_WHITELIST).permitAll()
                    .antMatchers("/extraction-survey-unit/**").hasRole(adminRP)
                // Zone open bar
                .and()
                .formLogin().permitAll().and().logout().permitAll()
                .and().oauth2ResourceServer().jwt();
                http.csrf().disable();
                return http.build();*/
                http
                    .authorizeRequests(authorize -> authorize
                        .antMatchers(SWAGGER_WHITELIST).permitAll()
                        .antMatchers("/extraction-survey-unit/**").hasRole(adminRP)
                    )
                    .formLogin(form -> form.permitAll())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(jwtAuthenticationConverter()));
                    /*.exceptionHandling().accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.info("AccessDenied (from spring security) uri={} -  user={}  ", request.getRequestURI(),request.getUserPrincipal());
                            throw accessDeniedException;
                    });*/
                return http.build();
        }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
                jwtAuthenticationConverter.setPrincipalClaimName(oidcClaimUsername);
                jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
                return jwtAuthenticationConverter;
        }

        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
                return new Converter<Jwt, Collection<GrantedAuthority>>() {
                        @Override @SuppressWarnings({ "unchecked" }) public Collection<GrantedAuthority> convert(Jwt source) {

                                String[] claimPath = oidcClaimRole.split("\\.");
                                Map<String, Object> claims = source.getClaims();
                                try {

                                        for (int i = 0; i < claimPath.length - 1; i++) {
                                                claims = (Map<String, Object>) claims.get(claimPath[i]);
                                        }

                                        List<String> roles = (List<String>) claims.getOrDefault(claimPath[claimPath.length - 1], new ArrayList<>());
                                        //if we need to add customs roles to every connected user we could define this variable (static or from properties)
                                        //roles.addAll(defaultRolesForUsers);
                                        return roles.stream().map(s -> new GrantedAuthority() {
                                                @Override public String getAuthority() {
                                                        return ROLE_PREFIX + s;
                                                }

                                                @Override public String toString() {
                                                        return getAuthority();
                                                }
                                        }).collect(Collectors.toList());
                                }
                                catch (ClassCastException e) {
                                        // role path not correctly found, assume that no role for this user
                                        return new ArrayList<>();
                                }
                        }
                };
        }
}


