package fr.insee.era.extraction_rp_famille.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RIMEntity {

        BDDSource bdd_source;
        //Besoin attesté pour CSV
        Long   id;              //CSV : Identifiant -  Contrainte : doit être unique dans le CSV Produit
        //String identifiant;
        String numvoiloc;       //CSV : NumeroVoie
        String typevoiloc;      //CSV : TypeVoie
        String nomvoiloc;       //CSV : LibelleVoie
        String cpostloc;        //CSV : CodePostal
        String cloc;            //CSV : LibelleCommune

        //Besoin interne au traitement

        //A priori inutiles

        /*String etat;
        Timestamp dateEnvoi;
        String irisar;
        String rgaar;
        String rglar;
        String dar;
        String carcode;
        String car;*/

}
