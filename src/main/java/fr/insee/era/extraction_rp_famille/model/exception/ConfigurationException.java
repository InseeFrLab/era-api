package fr.insee.era.extraction_rp_famille.model.exception;

public class ConfigurationException extends Exception{

        public ConfigurationException(String msg) {
                super("Erreur de configuration:  ");
        }

}
