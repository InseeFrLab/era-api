package fr.insee.era.extraction_rp_famille.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.era.extraction_rp_famille.configuration.OdicDataSourceConfiguration;
import fr.insee.era.extraction_rp_famille.configuration.OmerDataSourceConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.Resource;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.model.exception.CommuneInconnueException;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.model.exception.RimInconnueException;
import fr.insee.era.extraction_rp_famille.service.ExtractionServiceCSV;
import fr.insee.era.extraction_rp_famille.service.ExtractionServiceJSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.Date;
import java.util.Collection;

@Controller
@Slf4j
@RequestMapping("/extraction-survey-unit")
@SecurityRequirement(name = "keycloak")
public class ExtractionController {

        @Autowired ExtractionServiceJSON extractionServiceJSON;
        @Autowired ExtractionServiceCSV extractionServiceCSV;

        @Value("${spring.datasource.odic.url}")
        String odicURL;

        @Value("${spring.datasource.omer.url}")
        String homereURL;

        @GetMapping(value="/survey-units-for-period")
        @Operation(summary = "Récupération des répondants aux RP d'une période")
        public ResponseEntity<Collection<ReponseListeUEDto>>  getAllSUForPeriod(@RequestParam("startDate") Date dateDebut, @RequestParam("endDate") Date dateFin)
            throws DataAccessException, ConfigurationException {
                log.info("getAllSUForPeriod utilisateur={} dateDebut={} dateFin={} ",SecurityContextHolder.getContext().getAuthentication().getName(), dateDebut, dateFin);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getAllRimForPeriod(dateDebut, dateFin));
        }

        @GetMapping(value="/{id}")
        @Operation(summary = "Récupération d'une unité enquêtée par son identifiant")
        public ResponseEntity<ObjectNode>  getSU(@PathVariable("id") Long ueId, @RequestParam("idCampaign ") String questionnaireId)
            throws Exception {
                log.info("getSU utilisateur={} id={} questionnaireId={} ",SecurityContextHolder.getContext().getAuthentication().getName(), ueId, questionnaireId);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getDataForRim(ueId,questionnaireId));
        }


        @GetMapping(value="/survey-units-for-period-csv" , produces = "text/csv")
        @Operation(summary = "Récupération de toutes les unités enquêtées d'une période")
        public ResponseEntity<Resource>  getAllSUForPeriodAsCSV(@RequestParam("dateDebut") Date dateDebut, @RequestParam("dateFin") Date dateFin,@RequestParam("idCampaign ") String questionnaireId)
            throws IOException, RimInconnueException, CommuneInconnueException, ConfigurationException {
                ByteArrayOutputStream csvByteArrayOutputStream = extractionServiceCSV.getAllRimForPeriodAsCSV(dateDebut, dateFin,questionnaireId);
                Resource ressource = new ByteArrayResource(csvByteArrayOutputStream.toByteArray());

                String fileName = String.format("extraction_%s_%s_%s.csv",questionnaireId,dateDebut,dateFin);
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(ressource);
        }

        @GetMapping(value="/info-bdd")
        @Operation(summary = "Informations sur les bases de données du RP utilisées")
        public ResponseEntity<String>  getInfoBDD(){
                return ResponseEntity.ok(
                    """
                         BDD HOMERE:   %s
                         BDD ODIC  :   %s
                    """
                        .formatted(homereURL,odicURL)
                );
        }
}
