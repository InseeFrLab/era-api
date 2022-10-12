package fr.insee.era.extraction_rp_famille.model.exception;

public class RimInconnueException extends Exception{
        public RimInconnueException(Long idRim) {
                super("Pas de réponse internet ménage connue pour id="+idRim);
        }
}
