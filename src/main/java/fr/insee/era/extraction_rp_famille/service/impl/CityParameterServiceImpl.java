package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.model.dto.CityParameterDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.service.CityParameterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class CityParameterServiceImpl implements CityParameterService {

    ParametrageConfiguration parametrageConfiguration;
    public CityParameterServiceImpl(ParametrageConfiguration parametrageConfiguration) {
        this.parametrageConfiguration = parametrageConfiguration;
    }
    @Override
    public List<CityParameterDto> getCityParameters() {
        List<String> malesCities = parametrageConfiguration.getCommunesHommes();
        List<String> femalesCities = parametrageConfiguration.getCommunesFemmes();
        List<String> malesIris = parametrageConfiguration.getIrisHommes();
        List<String> femalesIris = parametrageConfiguration.getIrisFemmes();

        if (!isValid(malesIris) || !isValid(femalesIris)) {
            throw new ConfigurationException("Invalid iris parameters");
        }

        List<CityParameterDto> malesCitiesParameter = malesCities.stream().map(mc ->
                CityParameterDto.builder().cityCode(mc).gender(GenderType.MALE).build()).toList();
        List<CityParameterDto> femalesCitiesParameter = femalesCities.stream().map(mc ->
                CityParameterDto.builder().cityCode(mc).gender(GenderType.FEMALE).build()).toList();
        List<CityParameterDto> malesIrisParameter = malesIris.stream().map(mc -> mc.split("-")).map(mc ->
                CityParameterDto.builder().cityCode(mc[0]).iris(mc[1]).gender(GenderType.MALE).build()).toList();
        List<CityParameterDto> femalesIrisParameter = femalesIris.stream().map(mc -> mc.split("-")).map(mc ->
                CityParameterDto.builder().cityCode(mc[0]).iris(mc[1]).gender(GenderType.FEMALE).build()).toList();

        List<CityParameterDto> cityParameterEntities = new ArrayList<>();
        cityParameterEntities.addAll(malesCitiesParameter);
        cityParameterEntities.addAll(femalesCitiesParameter);
        cityParameterEntities.addAll(malesIrisParameter);
        cityParameterEntities.addAll(femalesIrisParameter);

        return cityParameterEntities;
    }

    private boolean isValid(List<String> iris) {
        // Validation format <cityCode>-<iris>
        if (iris == null || iris.isEmpty()) {
            return false;
        }
        long countDifferentThanTwo = iris.stream().map(i -> i.split("-")).filter(i-> i.length != 2).count();
        return countDifferentThanTwo == 0;
    }
}
