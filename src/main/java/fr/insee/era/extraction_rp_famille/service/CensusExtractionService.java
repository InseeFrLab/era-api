package fr.insee.era.extraction_rp_famille.service;

import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;

import java.time.LocalDate;
import java.util.List;

public interface CensusExtractionService {

    List<ResponseNetUserDto> getCensusRespondents(LocalDate startDate, LocalDate endDate, GenderType gender);
}
