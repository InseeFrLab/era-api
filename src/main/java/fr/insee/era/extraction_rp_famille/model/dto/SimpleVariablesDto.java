package fr.insee.era.extraction_rp_famille.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SimpleVariablesDto {
    @JsonProperty("RPLISTPRENOMS")
    private String rpListePrenoms;
    @JsonProperty("TYPE_QUEST")
    private String typeQuest;
    @JsonProperty("RPTYPEQUEST")
    private String rpTypeQuest;
    @JsonProperty("RPNBQUEST")
    private String rpNbQuest;
}
