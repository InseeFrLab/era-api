package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SimpleVariable {
    private String name;
    private String value;
}
