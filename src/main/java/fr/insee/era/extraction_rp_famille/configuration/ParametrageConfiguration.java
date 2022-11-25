package fr.insee.era.extraction_rp_famille.configuration;

import fr.insee.era.extraction_rp_famille.model.Constantes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration @PropertySource(value = "classpath:parametrage.properties")
@Getter @Slf4j

public class ParametrageConfiguration implements InitializingBean {

        private static Boolean isInitialised = false;
        private static Map<String, Constantes.BI_SEXE> communesSansIRIS;
        private static Map<Pair<String, String>, Constantes.BI_SEXE> communesAvecIris;

        @Value("#{'${communes.hommes}'.split(',')}") private List<String> communesHommes;

        @Value("#{'${communes.femmes}'.split(',')}") private List<String> communesFemmes;

        @Value("#{'${iris.hommes}'.split(',')}") private List<String> irisHommes;

        @Value("#{'${iris.femmes}'.split(',')}") private List<String> irisFemmes;

        /**
         * Détermine le sexe associé à soit un code commune  (priorité 1) soit à un iris (si pas de reconnaissance par code commune)
         *
         * @param codeCommuneComplet code insee de la commune (concat codedep codecommune)
         * @param iris               iris (ou irisar dans table rim)
         * @return le sexe si la commune/iris est connu - null sinon
         */
        public Constantes.BI_SEXE getSexeForCommuneIris(String codeCommuneComplet, String iris) {
                if (!isInitialised) {
                        communesSansIRIS = new HashMap<>();
                        communesHommes.stream().filter(x -> !x.isEmpty()).forEach(codeCommune -> communesSansIRIS.put(codeCommune, Constantes.BI_SEXE.BI_SEXE_HOMME));
                        communesFemmes.stream().filter(x -> !x.isEmpty()).forEach(codeCommune -> communesSansIRIS.put(codeCommune, Constantes.BI_SEXE.BI_SEXE_FEMME));

                        communesAvecIris = new HashMap<>();
                        irisHommes.stream().filter(x -> !x.isEmpty()).forEach(conf -> {
                                var details = conf.split("-");
                                if (details.length != 2) {
                                        String message = "La conf d'un des iris homme n'est pas du type <codeCommune>-<IRIS>";
                                        log.error(message);
                                        System.exit(-1);
                                }
                                communesAvecIris.put(new ImmutablePair<>(details[0], details[1]), Constantes.BI_SEXE.BI_SEXE_HOMME);
                        });

                        irisFemmes.stream().filter(x -> !x.isEmpty()).forEach(conf -> {
                                var details = conf.split("-");
                                if (details.length != 2) {
                                        String message = "La conf d'un des iris femme n'est pas du type <codeCommune>-<IRIS>";
                                        log.error(message);
                                        System.exit(-1);
                                }
                                communesAvecIris.put(new ImmutablePair<>(details[0], details[1]), Constantes.BI_SEXE.BI_SEXE_FEMME);
                        });
                        isInitialised = true;
                }
                Constantes.BI_SEXE res;
                res = communesSansIRIS.get(codeCommuneComplet);
                if (res == null) {
                        res=communesAvecIris.get(new ImmutablePair<>(codeCommuneComplet, iris));
                }
                return res;
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