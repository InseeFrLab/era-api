package fr.insee.era.extraction_rp_famille.controller;

import fr.insee.era.extraction_rp_famille.adapter.CensusCsvAdapter;
import fr.insee.era.extraction_rp_famille.adapter.CensusJsonAdapter;
import fr.insee.era.extraction_rp_famille.model.dto.CensusSurveyUnitDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.CensusExtractionService;
import fr.insee.era.extraction_rp_famille.utils.CsvUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequestMapping("/census-extraction")
@SecurityRequirement(name = "keycloak")
public class CensusExtractionController {

    CensusExtractionService censusExtractionService;

    CensusJsonAdapter censusJsonAdapter;

    CensusCsvAdapter censusCsvAdapter;

    public CensusExtractionController(CensusExtractionService censusExtractionService, CensusJsonAdapter censusJsonAdapter, CensusCsvAdapter censusCsvAdapter) {
        this.censusExtractionService = censusExtractionService;
        this.censusJsonAdapter = censusJsonAdapter;
        this.censusCsvAdapter = censusCsvAdapter;
    }
    @GetMapping(value = "/census-respondents-by-period-and-gender")
    @Operation(summary = "Get census respondents for period by gender Male or Female")
    public ResponseEntity<List<CensusSurveyUnitDto>> getCensusRespondents(@RequestParam("startDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate,
                                                                          @RequestParam("endDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate,
                                                                          @RequestParam("gender") GenderType gender) {
        log.info("Get Census Respondents {} from {} to {}", gender.getLabel(), startDate, endDate);
        List<ResponseNetUserDto> censusRespondents = censusExtractionService.getCensusRespondents(startDate,endDate,gender);
        log.info("GET /census-respondents-by-period-and-gender: Number of units {}", censusRespondents.size());
        List<CensusSurveyUnitDto> json = censusRespondents.stream().map(r -> censusJsonAdapter.convert(r, gender)).toList();
        return ResponseEntity.ok(json);
    }

    @GetMapping(value = "/census-respondents-by-period-and-gender/csv-download")
    @Operation(summary = "Get census respondents for period by gender Male or Female in csv format")
    public ResponseEntity<Object> downloadCensusRespondents(@RequestParam("startDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate,
                                                                    @RequestParam("endDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate,
                                                                    @RequestParam("gender") GenderType gender,
                                                                    @RequestParam("idCampaign") String idCampaign) {
        log.info("Get Census Respondents {} from {} to {} in csv format", gender.getLabel(), startDate, endDate);
        List<ResponseNetUserDto> censusRespondents = censusExtractionService.getCensusRespondents(startDate,endDate,gender);
        log.info("GET /census-respondents-by-period-and-gender/csv-download: Number of units {}", censusRespondents.size());
        List<String[]> csvList = new ArrayList<>();
        String[] header = censusCsvAdapter.writeHeader();
        csvList.add(header);
        List<String[]> lines = censusRespondents.stream().map(r -> censusCsvAdapter.convert(idCampaign,r, gender, header.length)).toList();
        csvList.addAll(lines);
        ByteArrayInputStream csvStream = CsvUtils.write(csvList);

        String fileName = String.format("extraction_%s_%S_%s_%s.csv",idCampaign, gender.getLabel() ,startDate ,endDate);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(csvStream));
    }

    @GetMapping(value = "/census-respondents-supplements-by-period-and-gender/csv-download")
    @Operation(summary = "Get census respondents supplements for period by gender Male or Female in csv format")
    public ResponseEntity<Object> downloadCensusRespondentsSupplements(@RequestParam("startDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate startDate,
                                                            @RequestParam("endDate") @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate endDate,
                                                            @RequestParam("gender") GenderType gender) {
        log.info("Get Census Respondents {} from {} to {} in csv format", gender.getLabel(), startDate, endDate);
        List<ResponseNetUserDto> censusRespondents = censusExtractionService.getCensusRespondents(startDate,endDate,gender);
        log.info("GET /census-respondents-supplements-by-period-and-gender/csv-download: Number of units {}", censusRespondents.size());
        List<String[]> csvList = new ArrayList<>();
        int maxPerson = censusRespondents.stream().map(ResponseNetUserDto::countSurveyedAndMajor).max(Long::compareTo).orElse(0L).intValue();
        int maxChildren = censusRespondents.stream().map(ResponseNetUserDto::maxNumberOfChildren).max(Long::compareTo).orElse(0L).intValue();
        String[] header = censusCsvAdapter.writeHeaderSupplements(maxPerson,maxChildren);
        csvList.add(header);
        List<String[]> lines = censusRespondents.stream().map(r -> censusCsvAdapter.convertSupplements(r, header.length, maxPerson,maxChildren)).toList();
        csvList.addAll(lines);
        ByteArrayInputStream csvStream = CsvUtils.write(csvList);

        String fileName = String.format("extraction_Supplements_%S_%s_%s.csv", gender.getLabel() ,startDate ,endDate);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv")).body(new InputStreamResource(csvStream));
    }


}
