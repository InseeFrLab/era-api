package fr.insee.era.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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


    @Value("${fr.insee.era.admin.role.rp}")
    private String administrateurRPRole;
    @Value("${fr.insee.era.admin.role.hors-rp}")
    private String administrateurHorsRPRole;


    //Par défaut, spring sécurity prefixe les rôles avec cette chaine
    private static final String ROLE_PREFIX = "ROLE_";

    @Autowired
    InseeSecurityTokenProperties inseeSecurityTokenProperties;

    //Liste d'URL sur lesquels on n'applique pas de sécurité (swagger; actuator...)
    @Value("#{'${fr.insee.sndil.starter.security.whitelist-matchers}'.split(',')}")
    private String[] whiteList;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable) //NOSONAR
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers(whiteList).permitAll()
                                .requestMatchers("/extraction-survey-unit/**").hasRole(administrateurRPRole)
                                .requestMatchers("/census-extraction/**").hasRole(administrateurRPRole)
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setPrincipalClaimName(inseeSecurityTokenProperties.getOidcClaimUsername());
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return new Converter<>() {
            @Override
            @SuppressWarnings({"unchecked"})
            public Collection<GrantedAuthority> convert(Jwt source) {

                String[] claimPath = inseeSecurityTokenProperties.getOidcClaimRole().split("\\.");
                Map<String, Object> claims = source.getClaims();
                try {

                    for (int i = 0; i < claimPath.length - 1; i++) {
                        claims = (Map<String, Object>) claims.get(claimPath[i]);
                    }

                    List<String> roles = (List<String>) claims.getOrDefault(claimPath[claimPath.length - 1],
                            new ArrayList<>());
                    //if we need to add customs roles to every connected user we could define this variable (static
                    // or from properties)
                    return roles.stream().map(s -> new GrantedAuthority() {
                        @Override
                        public String getAuthority() {
                            return ROLE_PREFIX + s;
                        }

                        @Override
                        public String toString() {
                            return getAuthority();
                        }
                    }).collect(Collectors.toCollection(ArrayList::new));
                } catch (ClassCastException e) {
                    // role path not correctly found, assume that no role for this user
                    return new ArrayList<>();
                }
            }
        };
    }
}


