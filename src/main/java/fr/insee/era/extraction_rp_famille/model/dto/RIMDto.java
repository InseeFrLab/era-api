package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class RIMDto {

        String courriel;
        String identifiantInternet;
        String codeCommune;
        String addresse;
        String iris;
}
