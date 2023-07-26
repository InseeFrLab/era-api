package fr.insee.era.extraction_rp_famille.controller;

import fr.insee.era.configuration.SecurityConfig;
import fr.insee.era.extraction_rp_famille.model.dto.CensusJsonDto;
import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.CensusExtractionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
@WebMvcTest(controllers = CensusExtractionController.class, excludeAutoConfiguration =
        OAuth2ResourceServerAutoConfiguration.class)
class CensusExtractionControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private CensusExtractionService censusExtractionService;

    @Test
    void shouldReturnJsonNodeFromListOfCensusDto() throws Exception {
        // Given
        CensusJsonDto censusJsonDto = CensusJsonDto.builder().id(123132131231l).idinternaute(12123132132l).numvoiloc("1").bisterloc("ter").typevoiloc(
                        "Rue")
                .nomvoiloc("Pierre et Marie Curie").resloc("303").cloc("Vandoeuvre-les-Nancy").cpostloc("54500")
                .mail("prenom.nom@gmail.com").identifiantCompte("XX1235YYY")
                .externals(null).build();
        List<IndividualFormDto> individualFormDtos = List.of(IndividualFormDto.builder().firstName("Jean").lastName("Michel").build());
        ResponseNetUserDto responseNetUserDto = ResponseNetUserDto.builder().id(123132131231l).idinternaute(12123132132l).numvoiloc("1").bisterloc("ter")
                .typevoiloc("Rue").nomvoiloc("Pierre et Marie Curie").resloc("303").cloc("Vandoeuvre-les-Nancy").cpostloc("54500")
                .mail("prenom.nom@gmail.com").individuals(individualFormDtos).build();
        given(censusExtractionService.getCensusRespondents(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2), GenderType.MALE))
                .willReturn(List.of(responseNetUserDto));

        // When
        MvcResult result = mvc.perform(get("/census-extraction/census-respondents-by-period-and-gender")
                        .param("startDate", "01-01-2023")
                        .param("endDate", "02-01-2023")
                        .param("gender", GenderType.MALE.toString()))
                        .andExpect(status().isOk()).andReturn();

        // Then
        String actual = result.getResponse().getContentAsString();
        String expected = "[{\"idinternaute\":\"YYYYYY\",\"numvoiloc\":\"1\",\"bisterloc\":\"ter\"," +
                "\"typevoiloc\":\"Rue\",\"nomvoiloc\":\"Pierre et Marie Curie\",\"resloc\":\"303\"," +
                "\"cloc\":\"Vandoeuvre-les-Nancy\",\"cpostloc\":\"54500\",\"idenq\":\"005386307\"," +
                "\"mail\":\"prenom.nom@gmail.com\",\"identifiant\":\"XX1235YYY\"," +
                "\"externals\":{\"EXTERNAL\":{\"FIRSTNAME1\":\"Jean\",\"FIRSTNAME2\":\"Michel\"}}}]";
        assertEquals(expected, actual);

    }
}
