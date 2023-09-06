package fr.insee.era.extraction_rp_famille.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fr.insee.era.extraction_rp_famille.model.serializer.ExternalSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CensusSurveyUnitDto {
    private Long id;
    private String numvoiloc;
    private String bisterloc;
    private String typevoiloc;
    private String nomvoiloc;
    private String resloc;
    private String car;
    private String cpostloc;
    private Long idinternaute;
    private String mail;
    private String identifiantCompte;
    @JsonSerialize(using = ExternalSerializer.class)
    private ExternalsVariables externals;
}
