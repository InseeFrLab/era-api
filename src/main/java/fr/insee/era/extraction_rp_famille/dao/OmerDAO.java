package fr.insee.era.extraction_rp_famille.dao;

import fr.insee.era.extraction_rp_famille.model.BDDSource;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.dto.RIMDto;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class OmerDAO extends CommonDAO {

        OmerDAO() {
                bddSource = BDDSource.OMER;
        }
        @Qualifier("omerJdbcTemplate") @Autowired private JdbcTemplate jdbcTemplate1;

        public List<ReponseListeUEDto> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin){
                log.info("getIdRIMetInternetForPeriod OMER");
                return getIdRIMetInternetForPeriod(dateDebut,dateFin,jdbcTemplate1);
        }

        public  List<BIEntity> getBiEtLiensForRim(
            Long rimId,
            Map<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId){

                return getBiEtLiensForRim(rimId, jdbcTemplate1, inoutConjointByIndividuID, inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);
        }

        @Override public RIMDto getRim(Long rimId) {
                return getRim(rimId, jdbcTemplate1);
        }
}
