package fr.insee.era.extraction_rp_famille.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BIEntity {

        BDDSource bdd_source;
        Long   id;
        String nom;
        String prenom;
        String anai;
        Constantes.BI_SEXE sexe;
        String communeId; //Concat codeDep codeCommune

}
