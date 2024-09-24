package fr.insee.era.extraction_rp_famille.service.impl.mock;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ParametrageConfigurationInMemory extends ParametrageConfiguration {

    private List<String> communesHommes = new ArrayList<>();

    private List<String> communesFemmes = new ArrayList<>();

    private List<String> irisHommes = new ArrayList<>();

    private List<String> irisFemmes = new ArrayList<>();

    public void initCommunes() {
        communesHommes.add("99998");
        communesHommes.add("69387");
        communesHommes.add("31065");
        communesFemmes.add("99999");
        communesFemmes.add("94017");
        communesFemmes.add("13208");
    }

    public void initIrisWithoutInvalid() {
        irisHommes.add("976611-0110");
        irisHommes.add("976611-0119");
        irisHommes.add("976611-011O");
        irisFemmes.add("976611-0336");
        irisFemmes.add("976611-0338");
        irisFemmes.add("976611-0708");
    }

    public void initIrisWithInvalid() {
        irisHommes.add("9766110110");
        irisFemmes.add("976611-0708");
    }
}
