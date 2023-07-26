package fr.insee.era.extraction_rp_famille.model.exception;

public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String msg) {
        super("Erreur de configuration: " + msg);
    }

}
