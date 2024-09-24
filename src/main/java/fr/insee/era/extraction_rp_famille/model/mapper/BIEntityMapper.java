package fr.insee.era.extraction_rp_famille.model.mapper;

import fr.insee.era.extraction_rp_famille.model.BDDSource;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class BIEntityMapper implements RowMapper<BIEntity> {

        final BDDSource source;
        public BIEntityMapper(BDDSource source){
                this.source=source;
        }
        public BIEntity mapRow(ResultSet resultSet, int i) throws SQLException {
                Constantes.BI_SEXE sexe = Constantes.BI_SEXE.fromString(resultSet.getString("sexe"));

                return
                    BIEntity.builder()
                        .bdd_source(source)
                        .id(resultSet.getLong("id"))
                        .nom(resultSet.getString("nom"))
                        .prenom(resultSet.getString("prenom"))
                        .anai(resultSet.getString("anai"))
                        .sexe(sexe)
                        //.feuillelogement(resultSet.getLong("feuillelogement"))
                        .build();
        }
}