package fr.insee.era.extraction_rp_famille.service;

import fr.insee.era.extraction_rp_famille.model.dto.CityParameterDto;

import java.util.List;

public interface CityParameterService {

    List<CityParameterDto> getCityParameters();
}
