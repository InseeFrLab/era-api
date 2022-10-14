//package fr.insee.era.extraction_rp_famille.model.mapper;
//
//import fr.insee.era.extraction_rp_famille.model.BDDSource;
//import fr.insee.era.extraction_rp_famille.model.RIMEntity;
//import org.springframework.jdbc.core.RowMapper;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//public class RIMEntityMapper implements RowMapper<RIMEntity> {
//
//        BDDSource source;
//        public RIMEntityMapper(BDDSource source){
//                this.source=source;
//        }
//        public RIMEntity mapRow(ResultSet resultSet, int i) throws SQLException {
//                return
//                    RIMEntity.builder()
//                        .bdd_source(source)
//                        .id(resultSet.getLong("id"))
//                        .numvoiloc(resultSet.getString("numvoiloc"))
//                        .typevoiloc(resultSet.getString("typevoiloc"))
//                        .nomvoiloc(resultSet.getString("nomvoiloc"))
//                        .cpostloc(resultSet.getString("cpostloc"))
//                        .cloc(resultSet.getString("cloc"))
//                        .build();
//        }
//}