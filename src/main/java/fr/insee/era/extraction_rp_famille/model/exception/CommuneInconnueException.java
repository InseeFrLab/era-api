package fr.insee.era.extraction_rp_famille.model.exception;

public class CommuneInconnueException extends Exception{

        public CommuneInconnueException(String identifiantCommune) {
                super("Commune "+identifiantCommune+" inconnue de la configuration");
        }

        public CommuneInconnueException(String identifiantCommune, String iris) {
                super("Iris Commune= "+identifiantCommune+" Iris="+iris+" inconnu de la configuration");
        }
}
