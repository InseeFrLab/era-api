package fr.insee.era.extraction_rp_famille.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.service.ExtractionServiceJSON;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.sql.Date;
import java.util.Collection;

@RestController
@Slf4j
@RequestMapping("/extraction-survey-unit")
@SecurityRequirement(name = "keycloak")
public class ExtractionController {

        @Autowired ExtractionServiceJSON extractionServiceJSON;


        @Autowired AccessToken accessToken;

  //      @PreAuthorize("isAuthenticated()")
 //       @RolesAllowed("user")
        @GetMapping(value="/survey-units-for-period")
        @Operation(summary = "Récupération des répondants aux RP d'une période")
        public ResponseEntity<Collection<ReponseListeUEDto>>  getAllSUForPeriod(@RequestParam("startDate") Date dateDebut, @RequestParam("endDate") Date dateFin)
            throws DataAccessException {
                log.info("getAllSUForPeriod utilisateur={} dateDebut={} dateFin={} ",accessToken.getPreferredUsername(), dateDebut, dateFin);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getAllRimForPeriod(dateDebut, dateFin));
        }

        @GetMapping(value="/{id}")
        @Operation(summary = "Récupération d'une unité enquêtée par son identifiant")
        public ResponseEntity<ObjectNode>  getSU(@PathVariable("id") Long ueId, @RequestParam("idCampaign ") String questionnaireId) throws Exception {
                log.info("getSU utilisateur={} id={} questionnaireId={} ",accessToken.getPreferredUsername(), ueId, questionnaireId);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getDataForRim(ueId,questionnaireId));
        }
}
