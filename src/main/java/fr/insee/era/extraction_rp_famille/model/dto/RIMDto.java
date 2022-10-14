package fr.insee.era.extraction_rp_famille.model.dto;

import fr.insee.era.extraction_rp_famille.model.BIEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
public class RIMDto {

        String courriel;
        String identifiantInternet;
        String codeCommune;
        String addresse;
}
