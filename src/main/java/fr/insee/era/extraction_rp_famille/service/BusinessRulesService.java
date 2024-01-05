package fr.insee.era.extraction_rp_famille.service;

import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;

import java.util.List;

public interface BusinessRulesService {

    List<IndividualFormDto> filterIndividual(List<IndividualFormDto> individuals, GenderType gender);

    List<ResponseNetUserDto> filterResponseNetUser(List<ResponseNetUserDto> responses);

    List<ResponseNetUserDto> completeResponseNetUser(List<ResponseNetUserDto> genderCitiesResponse);
}
