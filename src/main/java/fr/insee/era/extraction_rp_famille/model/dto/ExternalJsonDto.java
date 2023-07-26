package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class ExternalJsonDto {
    private List<Long> surveyedIds;
    private SimpleVariablesDto simpleVariableDto;
    private Map<Long,LoopVariablesDto> surveyedVariablesList;
    private Map<Long,LoopVariablesDto> conjointVariablesList;
    private Map<Long,List<LoopVariablesDto>> parentsVariablesList;
    private Map<Long,List<LoopVariablesDto>> enfantsVariablesList;

}
