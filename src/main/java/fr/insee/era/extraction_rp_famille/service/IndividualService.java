package fr.insee.era.extraction_rp_famille.service;

import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;

import java.util.List;

public interface IndividualService {

    List<ResponseNetUserDto> getIndividuals(List<ResponseNetUserDto> genderCitiesResponse, GenderType gender);
}
