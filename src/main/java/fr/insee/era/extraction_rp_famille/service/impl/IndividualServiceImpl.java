package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.dao.OdicDAO;
import fr.insee.era.extraction_rp_famille.dao.OmerDAO;
import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.BusinessRulesService;
import fr.insee.era.extraction_rp_famille.service.IndividualService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
    public List<IndividualFormDto> getIndividualsByResponseId(long id, GenderType gender) {
        // Get individuals from both tables
        List<IndividualFormDto> omerIndividuals = omerDao.getIndividualsByResponseId(id);
        List<IndividualFormDto> odicIndividuals = odicDao.getIndividualsByResponseId(id);
        List<IndividualFormDto> individuals = odicIndividuals;
        omerIndividuals.removeAll(omerIndividuals.stream().filter(omer ->
                odicIndividuals.stream().anyMatch(odic -> omer.getId() == odic.getId())).toList());
        individuals.addAll(omerIndividuals);
        // filter individuals by business rules
        individuals = businessRulesService.filterIndividual(individuals, gender);
        return individuals;
    }
}
