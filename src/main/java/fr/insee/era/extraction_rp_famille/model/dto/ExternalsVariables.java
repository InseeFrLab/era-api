package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ExternalsVariables {
    List<SimpleVariable> simpleVariables;
    List<LoopVariable> loopVariables;
}
