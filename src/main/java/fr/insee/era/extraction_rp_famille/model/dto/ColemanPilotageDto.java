package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ColemanPilotageDto {

        String adress;
        int batchNumber=1;
        String firstname;
        String lastname;
        String idCampaign; //paramètre filé par Protools
        String idContact; //<!!!!idintenfiant de connexion internet!!!!!!>  Laisser vide ou ne pas mettre  (renseigné par protools) ou remettre ?
        String idSu; //identifiant
}