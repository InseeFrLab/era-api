package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Builder
@Data
public class LoopVariable {
    private String name;
    private List<String> values;
}
