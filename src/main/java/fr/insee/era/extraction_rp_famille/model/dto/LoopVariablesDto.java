package fr.insee.era.extraction_rp_famille.model.dto;

import fr.insee.era.extraction_rp_famille.model.enums.RelationshipType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoopVariablesDto {
    private RelationshipType type;
    private Integer index;
    private String id;
    private String firstName;
    private String gender;
    private String birthMonth;
    private String birthDay;
    private String birthYear;
    private String dpnaicode;
    private String cnaif;
    private String cnaie;
    private String pnai;
}
