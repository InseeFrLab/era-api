package fr.insee.era.extraction_rp_famille.controller;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequestMapping("/parametrage-controller")
@SecurityRequirement(name = "keycloak")
public class ParametrageController {

    @Value("${spring.datasource.odic.url}")
    String odicURL;

    @Value("${spring.datasource.omer.url}")
    String homereURL;

    @Autowired
    ParametrageConfiguration parametrageConfiguration;

    @GetMapping(value="/info-parametrage")
    @Operation(summary = "Informations sur les bases de données du RP utilisées et le paramétrage des communes et iris")
    public ResponseEntity<String> getInfoParametreage(){
        return ResponseEntity.ok(
                """
                     BDD HOMERE:   %s
                     BDD ODIC  :   %s
                     COMMUNES HOMMES : %s
                     COMMUNES FEMMES : %s
                     IRIS HOMMES : %s
                     IRIS FEMMES : %s
                """
                        .formatted(homereURL,odicURL,
                                parametrageConfiguration.getCommunesHommes(),
                                parametrageConfiguration.getCommunesFemmes(),
                                parametrageConfiguration.getIrisHommes(),
                                parametrageConfiguration.getIrisFemmes()
                        )
        );
    }
}
