package fr.insee.era.extraction_rp_famille.dao;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.model.BDDSource;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.Constantes;
import fr.insee.era.extraction_rp_famille.model.dto.RIMDto;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.model.mapper.BIEntityMapper;
import fr.insee.era.extraction_rp_famille.model.mapper.RIMDtoMapper;
import fr.insee.era.extraction_rp_famille.model.mapper.ReponseListeUEDtoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j @Repository public abstract class CommonDAO {

        BDDSource bddSource; // A définir dans les sous classes
        @Autowired ParametrageConfiguration parametrageProperties;

        //-------------------------------------------------POUR JSON et COLEMAN----------------------------------------------------------//
        public abstract List<ReponseListeUEDto> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin) throws ConfigurationException;
        public abstract List<BIEntity> getBiEtLiensForRim(
            Long rimId,
            Map<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId);

        /**
         * Récupération des infos d'une RIM en base
         * @param rimId
         * @return le DTO ou Null si rimId n'existe pas en base
         */
        public abstract RIMDto getRim(Long rimId);
        protected RIMDto getRim(Long rimId, JdbcTemplate jdbc){

                try{

                        return jdbc.queryForObject("SELECT numvoiloc,typevoiloc,nomvoiloc,cpostloc,cloc "
                            + ", rim.identifiant , codedepartement||codecommune as code_commune_complet, irisar, mail "
                            +"  FROM reponseinternetmenages rim, internautes i "
                            +" where rim.idinternaute  = i.id and rim.id=?", new RIMDtoMapper(),rimId);


                }
                catch (        EmptyResultDataAccessException e) {
                        //La RIM n'existe pas dans cette base
                        return null;
                }
        }

        protected List<ReponseListeUEDto> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin, JdbcTemplate jdbc) throws ConfigurationException {

                jdbc.execute("DROP TABLE IF EXISTS tmp_era_communes_a_traiter_par_sexe ");
                jdbc.execute("CREATE TEMPORARY TABLE IF NOT EXISTS tmp_era_communes_a_traiter_par_sexe (code_commune_insee varchar(255) NOT NULL,  irisar varchar(255), sexe varchar(255) NOT NULL) ");

                List<Object[]> communesSexe = new ArrayList<>();
                for (String id : parametrageProperties.getCommunesFemmes()) {
                        communesSexe.add(new Object[] { id,null,Constantes.BI_SEXE.BI_SEXE_FEMME.toString() });
                }
                for (String id : parametrageProperties.getCommunesHommes()) {
                        communesSexe.add(new Object[] { id, null,Constantes.BI_SEXE.BI_SEXE_HOMME.toString() });
                }
                for (String irisFemme : parametrageProperties.getIrisFemmes()) {
                        var splittedData = irisFemme.split("-");
                        if(splittedData.length!=2){
                                String message = "La conf d'un des iris femme n'est pas du type <codeCommune>-<IRIS>";
                                log.error(message);
                                throw new ConfigurationException(message);
                        }
                        communesSexe.add(new Object[] { splittedData[0],splittedData[1],Constantes.BI_SEXE.BI_SEXE_FEMME.toString() });
                }
                for (String irisHomme : parametrageProperties.getIrisHommes()) {
                        var splittedData = irisHomme.split("-");
                        if(splittedData.length!=2){
                                String message = "La conf d'un des iris homme n'est pas du type <codeCommune>-<IRIS>";
                                log.error(message);
                                throw new ConfigurationException(message);
                        }
                        communesSexe.add(new Object[] { splittedData[0],splittedData[1],Constantes.BI_SEXE.BI_SEXE_HOMME.toString() });                }

                log.info("Insertion dans la table temporaire des communes ");
                jdbc.batchUpdate("INSERT INTO tmp_era_communes_a_traiter_par_sexe VALUES(?,?,?)", communesSexe);
                log.info("Recuperation des RIMs (ayant un mail renseigné)");
                String sql =
                    "select distinct r.id,r.identifiant,  b.sexe, i.mail"
                        + " from   reponseinternetmenages r, bulletinindividuels b ,  internautes i, tmp_era_communes_a_traiter_par_sexe tmp "
                        + " where   r.dateenvoi between ? and ? " + " and     r.codedepartement||r.codecommune  = tmp.code_commune_insee "
                        + " and (tmp.irisar is null or tmp.irisar=r.irisar) "
                        + " and     b.feuillelogement = r.id " + " and     b.tableauabcd='A' " + " and     b.sexe = tmp.sexe " + " and     b.anai <= '"
                        + Constantes.ANNEE_NAISSANCE_MAJEUR + "' "
                        + " and r.idinternaute  = i.id "
                        // enlever les personnes sans mail car pour famille on ne peut rien en faire
                        + " and (LTRIM(i.mail) <> '')  "
                    ;

                Timestamp debutTS = new Timestamp(dateDebut.getTime());
                Timestamp finTS = new Timestamp(dateFin.getTime());

                log.info("Récupération des rims entre " + debutTS + " et " + finTS);
                var list1 = jdbc.query(sql, new Object[] { debutTS, finTS }, new int[] { Types.TIMESTAMP, Types.TIMESTAMP },
                    new ReponseListeUEDtoMapper());

                jdbc.execute("TRUNCATE tmp_era_communes_a_traiter_par_sexe");
                return list1;
        }



        protected List<BIEntity> getBiEtLiensForRim(
            Long rimId,
            JdbcTemplate jdbcTemplate,
            Map<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId) {

                log.debug("Lecture en base pour RIM "+rimId);

                String sql =
                    "select id, nom, prenom, sexe, anai "
                    + " from bulletinindividuels "
                    + " where tableauabcd ='A' and feuillelogement=? ";
                List<BIEntity> bIEntityList = jdbcTemplate.query(sql, new BIEntityMapper(bddSource), rimId);

                List<Long> biIdList = bIEntityList.stream().map(BIEntity::getId).collect(Collectors.toList());
                if(!biIdList.isEmpty()) {
                        String inParams = String.join(",", biIdList.stream().map(id -> "?").collect(Collectors.toList()));

                        String sqlLien = String.format("SELECT individu, lienenregistre, individurelie " + " FROM lienindividus " + " WHERE lienenregistre in (1,2,3)"
                                + "        AND ( individu IN (%s) and individurelie in (%s) ) ", inParams, inParams);

                        List<Long> liste2X = biIdList;
                        liste2X.addAll(biIdList);
                        var liensList = jdbcTemplate.query(sqlLien,  (rs, i) -> Triple.of(rs.getLong(1), rs.getLong(2), rs.getLong(3)),
                            liste2X.toArray(Long[]::new));

                        for (Triple<Long, Long, Long> lien : liensList) {
                                Long middle = lien.getMiddle();
                                if (middle.equals(Constantes.LIEN_CONJOINT)) {
                                        //Si on a déjà un lien conjoint ==> On ignore les suivants de cet individu
                                        inoutConjointByIndividuID.putIfAbsent(lien.getLeft(), lien.getRight());
                                }
                                else if (middle.equals(Constantes.LIEN_PARENT)) {
                                        //individu est le parent de individurelie
                                        inoutLienParentByIndividuId.add(lien.getLeft(), lien.getRight());
                                }
                                else if (middle.equals(Constantes.LIEN_ENFANT)) {
                                        //individu est un enfant de individurelie
                                        inoutLienEnfantByIndividuId.add(lien.getLeft(), lien.getRight());
                                }
                        }

                        //suppression des liens où on n'a pas le mirroir
                        //Conjoint
                        inoutConjointByIndividuID.entrySet().removeIf(
                            entry ->
                            {
                                    Long conjointId = inoutConjointByIndividuID.get(entry.getKey());
                                    Long conjointDuConjoint = inoutConjointByIndividuID.get(conjointId);

                                        return !entry.getKey().equals(conjointDuConjoint);
                            });

                        nettoyageLiensParentsEnfants(inoutLienEnfantByIndividuId, inoutLienParentByIndividuId);
                        nettoyageLiensParentsEnfants(inoutLienParentByIndividuId, inoutLienEnfantByIndividuId);

                        //Enfant (un enfant peut indiquer deux relations seuleulement)
                        for (Long idEnfant : new ArrayList<>(inoutLienEnfantByIndividuId.keySet())) {
                                var parentList = inoutLienEnfantByIndividuId.get(idEnfant);
                                if (parentList.size() > 2) {
                                        List<Long> idParentsASupprimer = new ArrayList<>(parentList.subList(2, parentList.size()));
                                        for (Long idParent : idParentsASupprimer) {
                                                log.debug("Individu avec plus que deux parents. On supprime les liens entre individu=" + idEnfant
                                                    + " et individu=" + idParent);
                                                inoutLienParentByIndividuId.get(idParent).remove(idEnfant);
                                                inoutLienEnfantByIndividuId.get(idEnfant).remove(idParent);
                                        }
                                }
                        }
                }
                return bIEntityList;
        }


        private void nettoyageLiensParentsEnfants(LinkedMultiValueMap<Long,Long> mapA, LinkedMultiValueMap<Long,Long> mapB){
                //Pair :
                //      Left  : Key dans mapA
                //      Right : Value de mapA(left) à supprimer de la liste
                List<Pair<Long,Long>> liensASupprimer = new LinkedList<>();
                for ( Long individuId : new ArrayList<>(mapA.keySet())  ) {
                        for (Long individuRelieId: mapA.get(individuId)) {
                                if(!mapB.containsKey(individuRelieId) ||!mapB.get(individuRelieId).contains(individuId) ) {
                                        log.info("On supprime le lien orphelin individu=" + individuId + " individurelie=" + individuRelieId);
                                        liensASupprimer.add(new ImmutablePair<>(individuId, individuRelieId));
                                }
                        }
                }

                for (Pair<Long,Long> lienASupprimer : liensASupprimer  ) {
                        mapA.get(lienASupprimer.getLeft()).remove(lienASupprimer.getRight());
                }
        }
}