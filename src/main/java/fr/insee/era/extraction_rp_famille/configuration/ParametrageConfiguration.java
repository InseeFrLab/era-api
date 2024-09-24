package fr.insee.era.extraction_rp_famille.configuration;

import fr.insee.era.extraction_rp_famille.model.Constantes;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration @PropertySource(value = "classpath:parametrage.properties")
@Getter @Slf4j

public class ParametrageConfiguration implements InitializingBean {

        @Value("#{'${communes.hommes}'.split(',')}") private List<String> communesHommes;

        @Value("#{'${communes.femmes}'.split(',')}") private List<String> communesFemmes;

        @Value("#{'${iris.hommes}'.split(',')}") private List<String> irisHommes;

        @Value("#{'${iris.femmes}'.split(',')}") private List<String> irisFemmes;

        /**
         * Supprime les éventuels blancs dans communes et iris
         */
        @PostConstruct
        private void init() {
                this.communesHommes = communesHommes.stream().map(String::trim).toList();
                this.communesFemmes = communesFemmes.stream().map(String::trim).toList();
                this.irisHommes = irisHommes.stream().map(String::trim).toList();
                this.irisFemmes = irisFemmes.stream().map(String::trim).toList();
        }

        @Override public void afterPropertiesSet() {
                log.info("================================ Properties : Parametrage =================================");
                log.info("Communes hommes : {} ", communesHommes);
                log.info("Communes femmes : {} ", communesFemmes);
                log.info("Iris hommes : {} ", irisHommes);
                log.info("Iris femmes : {} ", irisFemmes);
                log.info("============================================================================");

        }
}