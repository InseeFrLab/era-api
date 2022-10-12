package fr.insee.era.extraction_rp_famille.model.exception;

public class CommuneInconnueException extends Exception{

        public CommuneInconnueException(String identifiantCommune) {
                super("Commune "+identifiantCommune+" inconnue de la configuration");
        }

}
