package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class IndividualFormDto {
    private long id;
    private String lastName;
    private String firstName;
    private String gender;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String dpnaicode;
    private String cnaif;
    private String cnaie;
    private String pnai;
    private boolean surveyed;
    private boolean major;
    private List<RelationshipDto> familyTies;

}
