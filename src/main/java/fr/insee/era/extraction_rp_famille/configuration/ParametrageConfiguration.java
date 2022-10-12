package fr.insee.era.extraction_rp_famille.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource(value="classpath:parametrage.properties")
@Getter
public class ParametrageConfiguration {

        @Value("#{'${communes.hommes}'.split(',')}")
        private List<String> communesHommes;

        @Value("#{'${communes.femmes}'.split(',')}")
        private List<String> communesFemmes;
}