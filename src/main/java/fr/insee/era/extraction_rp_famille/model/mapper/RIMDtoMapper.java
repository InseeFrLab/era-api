package fr.insee.era.extraction_rp_famille.model.mapper;//package fr.insee.era.extraction_rp_famille.model.mapper;

import fr.insee.era.extraction_rp_famille.model.dto.RIMDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RIMDtoMapper implements RowMapper<RIMDto> {


        public RIMDto mapRow(ResultSet resultSet, int i) throws SQLException {
                return
                    RIMDto.builder()
                        //.addresse(resultSet.getString("adresse"))
                        .numvoiloc(resultSet.getString("numvoiloc"))
                        .typevoiloc(resultSet.getString("typevoiloc"))
                        .nomvoiloc(resultSet.getString("nomvoiloc"))
                        .cpostloc(resultSet.getString("cpostloc"))
                        .cloc(resultSet.getString("cloc"))

                        .courriel(resultSet.getString("mail"))
                        .codeCommune(resultSet.getString("code_commune_complet"))
                        .iris(resultSet.getString("irisar"))
                        .identifiantInternet(resultSet.getString("identifiant"))
                        .build();
        }
}