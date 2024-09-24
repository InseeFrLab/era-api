package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.model.dto.CityParameterDto;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.service.CityParameterService;
import fr.insee.era.extraction_rp_famille.service.impl.mock.ParametrageConfigurationInMemory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CityParameterServiceTest {

    ParametrageConfigurationInMemory parametrageConfiguration = new ParametrageConfigurationInMemory();

    CityParameterService cityParameterService = new CityParameterServiceImpl(parametrageConfiguration);

    @Test
    void shouldReturnCityParametersWithoutInvalidIris() {
        // Given
        parametrageConfiguration.initIrisWithoutInvalid();
        parametrageConfiguration.initCommunes();

        // When
        List<CityParameterDto> cityParameters = cityParameterService.getCityParameters();

        // Then
        assertThat(cityParameters).hasSize(12);
    }

    @Test
    void shouldReturnConfigurationExceptionWithInvalidIris() {
        // Given
        parametrageConfiguration.initIrisWithInvalid();
        parametrageConfiguration.initCommunes();

        // When + Then
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> cityParameterService.getCityParameters());
        Assertions.assertEquals("Erreur de configuration: Invalid iris parameters", exception.getMessage());
    }


}
