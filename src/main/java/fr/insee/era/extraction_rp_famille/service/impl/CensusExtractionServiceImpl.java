package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.dao.OdicDAO;
import fr.insee.era.extraction_rp_famille.dao.OmerDAO;
import fr.insee.era.extraction_rp_famille.model.dto.CityParameterDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.BusinessRulesService;
import fr.insee.era.extraction_rp_famille.service.CensusExtractionService;
import fr.insee.era.extraction_rp_famille.service.CityParameterService;
import fr.insee.era.extraction_rp_famille.service.IndividualService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class CensusExtractionServiceImpl implements CensusExtractionService {
    ParametrageConfiguration parametrageConfiguration;
    CityParameterService cityParameterService;
    BusinessRulesService businessRulesService;
    IndividualService individualService;
    OmerDAO omerDao;
    OdicDAO odicDao;
    public CensusExtractionServiceImpl(ParametrageConfiguration parametrageConfiguration, OmerDAO omerDao, OdicDAO odicDAO) {
        this.parametrageConfiguration = parametrageConfiguration;
        this.cityParameterService = new CityParameterServiceImpl(parametrageConfiguration);
        this.individualService = new IndividualServiceImpl(omerDao, odicDAO);
        this.businessRulesService = new BusinessRulesServiceImpl();
        this.omerDao = omerDao;
        this.odicDao = odicDAO;
    }

    @Override
    public List<ResponseNetUserDto> getCensusRespondents(LocalDate startDate, LocalDate endDate, GenderType gender) {

        // Create temporary table for cities parameter
        List<CityParameterDto> cityParameterEntities = cityParameterService.getCityParameters();
        omerDao.createTemporaryTableForCitiesParameter(cityParameterEntities);
        log.info("Temporary table for cities parameter created with {} cities parameters", cityParameterEntities.size());

        // Get census respondents by gender city and period
        List<ResponseNetUserDto> genderCitiesResponse = omerDao.getRimByGenderCityAndPeriod(gender, startDate, endDate);
        log.info("Number of units {} in the census from {} to {}: {}", gender.getLabel(), startDate, endDate, genderCitiesResponse.size());

        // Get individuals
        genderCitiesResponse = individualService.getIndividuals(genderCitiesResponse, gender);

        // filter responses by business rules
        genderCitiesResponse = businessRulesService.filterResponseNetUser(genderCitiesResponse);
        return genderCitiesResponse;
    }
}
