package fr.insee.era;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.StreamSupport;

@SpringBootApplication
@Slf4j
public class EraApplication {

        public static void main(String[] args) {
                SpringApplication.run(EraApplication.class, args);
        }

        @Bean
        @Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
        public AccessToken getAccessToken() {
                HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                KeycloakSecurityContext securityContext = (KeycloakSecurityContext) httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
                if (securityContext != null) {
                        return securityContext.getToken();
                }
                else {
                        return new AccessToken();
                }
        }


        @EventListener
        public void handleContextRefresh(ContextRefreshedEvent event) {

                final Environment env = event.getApplicationContext().getEnvironment();

                log.info("================================ Properties =================================");
                final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
                StreamSupport.stream(sources.spliterator(), false).filter(EnumerablePropertySource.class::isInstance)
                    .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames()).flatMap(Arrays::stream).distinct()
                    .filter(prop -> !(prop.contains("credentials") || prop.contains("password") || prop.contains("pw")))
                    .filter(prop -> prop.startsWith("fr.insee")
                        || prop.startsWith("logging")
                        || prop.startsWith("keycloak")
                        || prop.startsWith("spring")
                        || prop.startsWith("application")
                        || prop.startsWith("project"))
                    .sorted().forEach(prop -> log.info("{}: {}", prop, env.getProperty(prop)));
                log.info("============================================================================");
        }

        @EventListener
        public void handleApplicationReady(ApplicationReadyEvent event) {
                log.info("=============== ERA  has successfully started. ===============");

        }


}
