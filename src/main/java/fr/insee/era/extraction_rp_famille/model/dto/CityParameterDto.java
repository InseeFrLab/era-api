package fr.insee.era.extraction_rp_famille.model.dto;


import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class CityParameterDto {

    private String cityCode;
    private String iris;
    private GenderType gender;
}
