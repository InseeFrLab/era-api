package fr.insee.era;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;

import java.util.Arrays;
import java.util.stream.StreamSupport;

@SpringBootApplication
@Slf4j
public class EraApplication  {

        public static void main(String[] args) {
                SpringApplication.run(EraApplication.class, args);
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
