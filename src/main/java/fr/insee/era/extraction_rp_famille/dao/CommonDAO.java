package fr.insee.era.extraction_rp_famille.dao;

import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.model.BDDSource;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.Constantes;
import fr.insee.era.extraction_rp_famille.model.mapper.BIEntityMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j @Repository public abstract class CommonDAO {

        BDDSource bddSource; // A définir dans les sous classes
        @Autowired ParametrageConfiguration parametrageProperties;
/*
        //A implémenter dans les sous classes
        public abstract void getBiEtLiensForRims(
            Collection<Long> rimKeys,
            List<BIEntity> outListOfBI,
            LinkedMultiValueMap<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutParentByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutEnfantByIndividuID);

        public abstract Long getMaxNbEnfant();

        public abstract Long getMaxNbAdulteMemeSexe();

        public abstract List<RIMEntity> getAllRIMForSexe(Date dateDebut, Date dateFin, Constantes.BI_SEXE sexe);

        protected Long getMaxNbEnfant(JdbcTemplate jdbcTemplate) {
                String sql = "select count(*) as nb from lienindividus l " + " where  l.lienenregistre  = " + Constantes.LIEN_PARENT
                    + " group by individu, lienenregistre" + " order by count(*) desc " + " limit 1";

                List<Long> longList = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1));
                return (longList.isEmpty()) ? 0l : longList.get(0);
        }

        protected Long getMaxNbAdulteMemeSexe(JdbcTemplate jdbcTemplate) {

                String sql = " select count(*) as nb " + " from bulletinindividuels b " + " where anai <= '" + Constantes.ANNEE_NAISSANCE_MAJEUR + "'"
                    + " group by b.feuillelogement , b.sexe " + " order by count(*) desc " + " limit 1";

                List<Long> longList = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1));
                return (longList.isEmpty()) ? 0l : longList.get(0);
        }

        protected List<RIMEntity> getAllRIMForSexe(Date dateDebut, Date dateFin, JdbcTemplate jdbc, Constantes.BI_SEXE sexe) {

                //TODO : pourquoi ca disparait à un moment la table temporaire?
                jdbc.execute("DROP TABLE IF EXISTS tmp_rem_communes_a_traiter ");
                jdbc.execute("CREATE TEMPORARY TABLE IF NOT EXISTS tmp_rem_communes_a_traiter (id varchar(255) NOT NULL)");

                List<Object[]> codesCommunes = new ArrayList<>();
                switch (sexe) {
                        case BI_SEXE_HOMME: {
                                log.info("SEXE=HOMMES");
                                for (String id : parametrageProperties.getCommunesHommes()) {
                                        codesCommunes.add(new Object[] { id });
                                }
                        }
                        break;
                        case BI_SEXE_FEMME: {
                                log.info("SEXE=FEMMES");
                                for (String id : parametrageProperties.getCommunesFemmes()) {
                                        codesCommunes.add(new Object[] { id });
                                }
                        }
                        break;
                }

                log.info("Insertion dans la table temporaire des communes ");
                jdbc.batchUpdate("INSERT INTO tmp_rem_communes_a_traiter VALUES(?)", codesCommunes);
                log.info("Recuperation des RIMs ");

                //TODO a supprimer (c'est juste du log)
                var tmp = jdbc.queryForList("SELECT id FROM tmp_rem_communes_a_traiter", String.class);
                log.info("tmp_rem_communes_a_traiter=" + ((tmp.size() > 10) ? (tmp.subList(0, 10) + "...") : tmp));

                String sql = "select id, numvoiloc , typevoiloc ,nomvoiloc , cpostloc, cloc from reponseinternetmenages" + " where dateenvoi between ? and ?"
                    + " and codedepartement||codecommune  in (SELECT id FROM tmp_rem_communes_a_traiter) ";

                Timestamp debutTS = new Timestamp(dateDebut.getTime());
                Timestamp finTS = new Timestamp(dateFin.getTime());

                log.info("Récupération des rims entre " + debutTS + " et " + finTS);
                List list1 = jdbc.query(sql, new Object[] { debutTS, finTS }, new int[] { Types.TIMESTAMP, Types.TIMESTAMP }, new RIMEntityMapper(bddSource));

                jdbc.execute("TRUNCATE tmp_rem_communes_a_traiter");
                return list1;
        }

        protected void getBiEtLiensForRims(
            Collection<Long> rimKeys,
            JdbcTemplate jdbcTemplate,
            List<BIEntity> outListOfBI,
            LinkedMultiValueMap<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutParentByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutEnfantByIndividuID) {
                //TODO : tableauabcd filtrer. Juste sur les enquêtes ou aussi sur les enfants??

                //TODO : WTF la table temporaire disparait à un moment avec CREATE TEMPORARY TABLE IF NOT EXISTS
                jdbcTemplate.execute("DROP TABLE IF EXISTS tmp_rem_rim_id");
                jdbcTemplate.execute("CREATE TEMPORARY TABLE tmp_rem_rim_id (id INT8 NOT NULL)");

                List<Object[]> rimIds = new ArrayList<>();
                for (Long id : rimKeys) {
                        rimIds.add(new Object[] { id });
                }

                //TODO : faire ca en batch de 100 ou 1000?
                log.info("Insertion dans la table temporaire ");
                jdbcTemplate.batchUpdate("INSERT INTO tmp_rem_rim_id VALUES(?)", rimIds);
                log.info("Recuperation des BI ");

                String sqlLiens =
                    " with bi as ( " + "         select id from bulletinindividuels b " + "         where b.feuillelogement IN (SELECT id FROM tmp_rem_rim_id) "
                        + "         and tableauabcd='A' " //A : occupants permanents
                        + "    )   " + "    select individu,lienenregistre,individurelie from lienindividus " + "    where individu in (select id from bi) "
                        + "    and lienenregistre in (1,2,3) ";

                var listOfLiens = jdbcTemplate.query(sqlLiens, (rs, i) -> Triple.of(rs.getLong(1), rs.getLong(2), rs.getLong(3)));

                for (Triple<Long, Long, Long> lien : listOfLiens) {
                        Long middle = lien.getMiddle();
                        if (middle == Constantes.LIEN_CONJOINT) {
                                //En base on a toujours 2 entrées (en mirroir)
                                inoutConjointByIndividuID.add(lien.getRight(), lien.getLeft());
                        }
                        else if (middle == Constantes.LIEN_PARENT) {
                                //individu est le parent de individurelie
                                inoutParentByIndividuID.add(lien.getRight(), lien.getLeft());
                        }
                        else // if (middle == Constantes.LIEN_ENFANT) {
                                //individu est un enfant de individurelie
                                inoutEnfantByIndividuID.add(lien.getRight(), lien.getLeft());
                }

                outListOfBI.addAll(jdbcTemplate.query("SELECT id,nom,prenom, anai, sexe, feuillelogement " + " FROM bulletinindividuels b"
                    + " WHERE feuillelogement IN (SELECT id FROM tmp_rem_rim_id) ", new BIEntityMapper(bddSource)));

                //TODO : si on reste sur un create or replace temporary, alors ca ne sert à rien
                jdbcTemplate.update("TRUNCATE  TABLE tmp_rem_rim_id");
                log.info("getBiEtLiensForRims: outListOfBI.size=" + outListOfBI.size());
                return;
        }
*/
        //-------------------------------------------------POUR JSON et COLEMAN----------------------------------------------------------//
        public abstract List<Pair<Long, String>> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin);
        public abstract List<BIEntity> getBiEtLiensForRim(
            Long rimId,
            Map<Long, Long> inoutConjointByIndividuID,
            LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId);

        public abstract Triple<String,String,String> getRim(Long rimId);

        //TODO : Commenter que ca renvoi null si on trouve rien
        protected  Triple<String,String,String> getRim(Long rimId, JdbcTemplate jdbc){
                try{
                        return jdbc.queryForObject("SELECT numvoiloc||' '||typevoiloc||' '||nomvoiloc||' '||cpostloc||' '||cloc"
                            + ", identifiant , codedepartement||codecommune "
                            +"  FROM reponseinternetmenages "
                            +" where id=?", (rs, rowNum) ->  Triple.of(rs.getString(1), rs.getString(2), rs.getString(3)),rimId);
                }
                catch (        EmptyResultDataAccessException e) {
                        //La RIM n'existe pas dans cette base
                        return null;
                }
        }

        protected List<Pair<Long, String>> getIdRIMetInternetForPeriod(Date dateDebut, Date dateFin, JdbcTemplate jdbc) {

                jdbc.execute("DROP TABLE IF EXISTS tmp_rem_communes_a_traiter ");
                jdbc.execute("CREATE TEMPORARY TABLE IF NOT EXISTS tmp_rem_communes_a_traiter_par_sexe (id varchar(255) NOT NULL, sexe varchar(255) NOT NULL)");

                List<Object[]> communesSexe = new ArrayList<>();
                for (String id : parametrageProperties.getCommunesFemmes()) {
                        communesSexe.add(new Object[] { id, Constantes.BI_SEXE.BI_SEXE_FEMME.toString() });
                }
                for (String id : parametrageProperties.getCommunesHommes()) {
                        communesSexe.add(new Object[] { id, Constantes.BI_SEXE.BI_SEXE_HOMME.toString() });
                }

                log.info("Insertion dans la table temporaire des communes ");
                jdbc.batchUpdate("INSERT INTO tmp_rem_communes_a_traiter_par_sexe VALUES(?,?)", communesSexe);
                log.info("Recuperation des RIMs ");

                //TODO a supprimer (c'est juste du log)
                var tmp = jdbc.queryForList("SELECT id FROM tmp_rem_communes_a_traiter_par_sexe", String.class);
                log.info("tmp_rem_communes_a_traiter_par_sexe=" + ((tmp.size() > 10) ? (tmp.subList(0, 10) + "...") : tmp));


                String sql =
                    "select distinct r.id,r.identifiant " + " from   reponseinternetmenages r, bulletinindividuels b , tmp_rem_communes_a_traiter_par_sexe tmp "
                        + " where   r.dateenvoi between ? and ? " + " and     r.codedepartement||r.codecommune  = tmp.id "
                        + " and     b.feuillelogement = r.id " + " and     b.tableauabcd='A' " + " and     b.sexe = tmp.sexe " + " and     b.anai <= '"
                        + Constantes.ANNEE_NAISSANCE_MAJEUR + "'";

                Timestamp debutTS = new Timestamp(dateDebut.getTime());
                Timestamp finTS = new Timestamp(dateFin.getTime());

                log.info("Récupération des rims entre " + debutTS + " et " + finTS);
                List<Pair<Long,String>> list1 = jdbc.query(sql, new Object[] { debutTS, finTS }, new int[] { Types.TIMESTAMP, Types.TIMESTAMP },
                    (rs, i) -> Pair.of(rs.getLong(1), rs.getString(2)));

                jdbc.execute("TRUNCATE tmp_rem_communes_a_traiter_par_sexe");
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
                                + "        AND ( individu IN (%s) or individurelie in (%s) ) ", inParams, inParams);

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
                for ( Long individuId : new ArrayList<>(mapA.keySet())  ) {
                        for (Long individuRelieId: mapA.get(individuId)) {
                                if(!mapB.get(individuRelieId).contains(individuId)){
                                        log.debug("On supprimer le lien orphelin PARENT individu="+individuId+" individurelie="+individuRelieId);
                                        mapA.get(individuId).remove(individuRelieId);
                                }
                        }
                }
        }
}