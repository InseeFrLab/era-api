package fr.insee.era.extraction_rp_famille.service;

import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;

import java.util.List;

public interface IndividualService {
    List<IndividualFormDto> getIndividualsByResponseId(long id, GenderType gender);
}
