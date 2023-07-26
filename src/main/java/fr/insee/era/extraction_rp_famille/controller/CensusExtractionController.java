package fr.insee.era.extraction_rp_famille.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.insee.era.extraction_rp_famille.adapter.CensusJsonAdapter;
import fr.insee.era.extraction_rp_famille.model.dto.CensusJsonDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.CensusExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/census-extraction")
@SecurityRequirement(name = "keycloak")
public class CensusExtractionController {

    CensusExtractionService censusExtractionService;

    CensusJsonAdapter censusJsonAdapter;

    public CensusExtractionController(CensusExtractionService censusExtractionService, CensusJsonAdapter censusJsonAdapter) {
        this.censusExtractionService = censusExtractionService;
        this.censusJsonAdapter = censusJsonAdapter;
    }
    @GetMapping(value = "/census-respondents-by-period-and-gender")
    @Operation(summary = "Get census respondents for period by gender 1==Male ; 2==Female")
    public ResponseEntity<List<CensusJsonDto>> getCensusRespondents(@RequestParam("startDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate,
                                                         @RequestParam("endDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate,
                                                         @RequestParam("gender") GenderType gender) {
        log.info("Get Census Respondents {} from {} to {}", gender.getLabel(), startDate, endDate);
        List<ResponseNetUserDto> censusRespondents = censusExtractionService.getCensusRespondents(startDate,endDate,gender);
        log.info("Number of units {}", censusRespondents.size());
        List<CensusJsonDto> json = censusRespondents.stream().map(r -> censusJsonAdapter.convert(r, gender)).toList();
        return ResponseEntity.ok(json);
    }
}
