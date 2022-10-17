package fr.insee.era.extraction_rp_famille.dao;

import fr.insee.era.extraction_rp_famille.configuration.OdicDataSourceConfiguration;
import fr.insee.era.extraction_rp_famille.model.BDDSource;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.dto.RIMDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class OdicDAO extends CommonDAO{

        OdicDAO(){
                bddSource=BDDSource.ODIC;
        }
        @Autowired
        private OdicDataSourceConfiguration conf;

        public List<Pair<Long,String>> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin){
                log.info("getIdRIMetInternetForPeriod ODIC");

                JdbcTemplate jdbcTemplate1 = new JdbcTemplate(conf.odicDataSource());

                return getIdRIMetInternetForPeriod(dateDebut,dateFin,jdbcTemplate1);
        }

        public  List<BIEntity> getBiEtLiensForRim(
            Long rimId,
            Map<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId){
                JdbcTemplate jdbcTemplate1 = new JdbcTemplate(conf.odicDataSource());

                return getBiEtLiensForRim(rimId, jdbcTemplate1, inoutConjointByIndividuID, inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);
        }

        @Override public RIMDto getRim(Long rimId) {

                JdbcTemplate jdbcTemplate1 = new JdbcTemplate(conf.odicDataSource());

                return getRim(rimId, jdbcTemplate1);
        }

}
