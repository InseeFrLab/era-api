package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReponseListeUEDto {

        Long id;
        String internaute;
        String mail;
        String sexe; //1 homme ; 2 femme
}
