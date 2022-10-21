package fr.insee.era.extraction_rp_famille.model.mapper;

import fr.insee.era.extraction_rp_famille.model.Constantes;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@AllArgsConstructor
@Getter
public class ReponseListeUEDtoMapper implements RowMapper<ReponseListeUEDto> {


        public ReponseListeUEDto mapRow(ResultSet resultSet, int i) throws SQLException {

                Constantes.BI_SEXE sexe = Constantes.BI_SEXE.fromString(resultSet.getString("sexe"));


                return
                    ReponseListeUEDto.builder()
                        .id(resultSet.getLong("id"))
                        .internaute(resultSet.getString("identifiant"))
                        .sexe(sexe.toString())
                        .build();
        }
}