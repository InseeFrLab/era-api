package fr.insee.era.extraction_rp_famille.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.service.ExtractionServiceJSON;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.util.Collection;

@RestController @Slf4j @RequestMapping("/extraction")
public class ExtractionController {

//        @Autowired ExtractionServiceCSV extractionServiceCSV;
        @Autowired ExtractionServiceJSON extractionServiceJSON;


        @Autowired AccessToken accessToken;
/*
        @GetMapping(value="/csv" , produces = "text/csv")
        public ResponseEntity<Resource>  getALLCSV(@RequestParam("dateDebut") Date dateDebut, @RequestParam("dateFin") Date dateFin)
            throws IOException, DataAccessException {
                File csvFile = extractionServiceCSV.extraireTOUTEtEcrire(dateDebut, dateFin);
                Resource resource = new FileSystemResource(csvFile);

                String fileName = "extraction"+dateDebut+"_"+dateFin+".csv";
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+fileName)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        }
*/
        @PreAuthorize("isAuthenticated()")
        @GetMapping(value="/getAllUeForPeriod")
        public ResponseEntity<Collection<ReponseListeUEDto>>  getAllUeForPeriodALLJSON(@RequestParam("dateDebut") Date dateDebut, @RequestParam("dateFin") Date dateFin)
            throws DataAccessException {


                log.info("getAllUeForPeriod utilisateur={} dateDebut={} dateFin={} ",accessToken.getPreferredUsername(), dateDebut, dateFin);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getAllRimForPeriod(dateDebut, dateFin));
        }

        @GetMapping(value="/getColemanJsonsForUe")
        @PreAuthorize("hasRole('CHIEF-OPERATING-OFFICER')")
        public ResponseEntity<ObjectNode>  getColemanJsonsForUe(@RequestParam("ueId") Long ueId, @RequestParam("questionnaireId") String questionnaireId) throws Exception {
                log.info("getAllUeForPeriod utilisateur={} ueId={} questionnaireId={} ",accessToken.getPreferredUsername(), ueId, questionnaireId);
                return ResponseEntity.status(HttpStatus.OK).body(extractionServiceJSON.getDataForRim(ueId,questionnaireId));
        }
}
