package fr.insee.era.extraction_rp_famille.model.exception;

import fr.insee.era.extraction_rp_famille.model.Constantes;

public class PasDeBIDuBonSexeException extends Exception{

        public PasDeBIDuBonSexeException(Long idRim, Constantes.BI_SEXE sexe) {
                super("Pas de BI majeur de sexe="+sexe+" pour le logement rimId="+idRim);
        }
}
