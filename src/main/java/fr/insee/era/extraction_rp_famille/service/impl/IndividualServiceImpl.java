package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.dao.OdicDAO;
import fr.insee.era.extraction_rp_famille.dao.OmerDAO;
import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.BusinessRulesService;
import fr.insee.era.extraction_rp_famille.service.IndividualService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class IndividualServiceImpl implements IndividualService {

    OmerDAO omerDao;
    OdicDAO odicDao;

    BusinessRulesService businessRulesService;

    public IndividualServiceImpl(OmerDAO omerDao, OdicDAO odicDao) {
        this.omerDao = omerDao;
        this.odicDao = odicDao;
        businessRulesService = new BusinessRulesServiceImpl();
    }

    @Override
    public List<ResponseNetUserDto> getIndividuals(List<ResponseNetUserDto> genderCitiesResponse, GenderType gender) {
        List<Long> ids = genderCitiesResponse.stream().map(ResponseNetUserDto::getId).toList();
        Map<Long, List<IndividualFormDto>> omerIndividuals = omerDao.getIndividuals(ids);
        Map<Long, List<IndividualFormDto>> odicIndividuals = odicDao.getIndividuals(ids);
        for (ResponseNetUserDto responseNetUserDto : genderCitiesResponse) {
            List<IndividualFormDto> odicIndividuelsList = odicIndividuals.get(responseNetUserDto.getId());
            List<IndividualFormDto> omerIndividuelsList = omerIndividuals.get(responseNetUserDto.getId());
            if (odicIndividuelsList == null) {
                odicIndividuelsList = List.of();
            }
            if (omerIndividuelsList == null) {
                omerIndividuelsList = List.of();
            }
            Map<Long, IndividualFormDto> individualsMap = odicIndividuelsList.stream().collect(Collectors.toMap(IndividualFormDto::getId, individual -> individual));
            for (IndividualFormDto individual : omerIndividuelsList) {
                if (!individualsMap.containsKey(individual.getId())) {
                    individualsMap.put(individual.getId(), individual);
                }
            }
            List<IndividualFormDto> individuals = individualsMap.values().stream().toList();
            individuals = businessRulesService.filterIndividual(individuals, gender);
            responseNetUserDto.setIndividuals(individuals);
        }
        return genderCitiesResponse;
    }
}
