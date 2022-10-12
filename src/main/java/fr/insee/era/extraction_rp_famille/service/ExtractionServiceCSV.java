//package fr.insee.extract_rp_famille.service;
//
//import com.opencsv.CSVWriter;
//import fr.insee.extract_rp_famille.dao.OdicDAO;
//import fr.insee.extract_rp_famille.dao.OmerDAO;
//import fr.insee.extract_rp_famille.model.BIEntity;
//import fr.insee.extract_rp_famille.model.Constantes;
//import fr.insee.extract_rp_famille.model.RIMEntity;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DataAccessException;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.sql.Date;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Slf4j @Service public class ExtractionServiceCSV {
//
//        static List<String> HEADER_RECORD = new ArrayList<String>(
//            Arrays.asList("Identifiant", "IdModele", "IdLot", "CiviliteReferent", "NomReferent", "PrenomReferent", "MailReferent", "NumeroVoie",
//                "IndiceRepetition", "TypeVoie", "LibelleVoie", "ComplementAdresse", "MentionSpeciale", "CodePostal", "LibelleCommune", "NomUe", "PrenomUe",
//                "AnneeNaissanceUe", "TYPE_QUEST", "RPTYPEQUEST", "RPNBQUEST"));
//        @Autowired OmerDAO omerDAO;
//        @Autowired OdicDAO odicDAO;
//
//        public File extraireTOUTEtEcrire(Date dateDebut, Date dateFin) throws DataAccessException, IOException {
//                //On doit adapter le header au nombre max de personnes et au nb max d'enfants
//                long maxNbPersonnesMajeures = getMaxNbAdulte();
//                long maxNbEnfant = getMaxNbEnfant();
//                updateHeader(maxNbEnfant, maxNbPersonnesMajeures);
//
//
//                //TODO : paramétrable
//                File file = new File("D:\\temp.csv");
//
//
//                for (Constantes.BI_SEXE sexe : Constantes.BI_SEXE.values()) {
//                        log.info("TRAITEMENT SEXE="+sexe);
//                        Map<Long, RIMEntity> toutesLesRIM = extraireRIM2BDD(dateDebut, dateFin, sexe);
//
//                        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
//                                //Ecriture du header CSV
//                                writer.writeNext(HEADER_RECORD.toArray(new String[HEADER_RECORD.size()]));
//
//                                //Traitement par paquets
//                                //TODO: à rendre paramètrable
//                                int TAILLE_PAQUET = 50 * 1000;
//                                List<Long> rimsRestantes = new ArrayList<Long>(toutesLesRIM.keySet());
//
//                                while (!rimsRestantes.isEmpty()) {
//                                        log.info("Nombre restant de rims à traiter = " + rimsRestantes.size());
//                                        int lastIndex = 0;
//                                        if (rimsRestantes.size() > TAILLE_PAQUET) {
//                                                lastIndex = TAILLE_PAQUET;
//                                        }
//                                        else {
//                                                lastIndex = rimsRestantes.size();
//                                        }
//                                        List<Long> rimIdsATraiter = rimsRestantes.subList(0, lastIndex);
//
//                                        ArrayListValuedHashMap<Long, BIEntity> BIByIDRIM = new ArrayListValuedHashMap<>();
//                                        Map<Long, BIEntity> individuByID = new HashMap<>();
//                                        LinkedMultiValueMap<Long, Long> conjointByIndividuID = new LinkedMultiValueMap<>();
//                                        LinkedMultiValueMap<Long, Long> parentByIndividuID = new LinkedMultiValueMap<>();
//                                        LinkedMultiValueMap<Long, Long> enfantByIndividuID = new LinkedMultiValueMap<>();
//                                        getBIEntityForRim(rimIdsATraiter, BIByIDRIM, individuByID, conjointByIndividuID, parentByIndividuID, enfantByIndividuID);
//
//                                        for (RIMEntity rim : toutesLesRIM.values()) {
//
//                                                if (BIByIDRIM.containsKey(rim.getId())) {
//                                                        List<BIEntity> biDeLaRim = BIByIDRIM.get(rim.getId());
//
//                                                        //Déjà as on un pax majeur du bon sexe???
//                                                        Set<BIEntity> biEnquetes =
//                                                            biDeLaRim.stream().filter(
//                                                                biEntity ->
//                                                                    ( Integer.valueOf(biEntity.getAnai()) <= Constantes.ANNEE_NAISSANCE_MAJEUR
//                                                                        && biEntity.getSexe()==sexe
//                                                                    )
//                                                                ).collect(Collectors.toSet());
//
//                                                        if(biEnquetes.isEmpty()){
//                                                                log.warn("Pas de personne majeure de sexe== "+sexe+" pour idRim="+rim.getId());
//                                                                continue;
//                                                        }
//
//                                                        String[] line = new String[HEADER_RECORD.size()];
//
//                                                        //Une seule ligne par logement
//                                                        int col = 0;
//                                                        line[col++] = String.valueOf(rim.getId()); //"Identifiant";
//                                                        line[col++] = null; //"IdModele";
//                                                        line[col++] = null; //"IDLot";
//                                                        line[col++] = null; //"CiviliteReferent";
//                                                        line[col++] = null; //"NomReferent";
//                                                        line[col++] = null; //"PrenomReferent";
//                                                        line[col++] = null; //"MailReferent";
//                                                        line[col++] = rim.getNumvoiloc();
//                                                        line[col++] = null; //"IndiceRepetition";
//                                                        line[col++] = rim.getTypevoiloc();
//                                                        line[col++] = rim.getNomvoiloc();
//                                                        line[col++] = null; //"ComplementAdresse";
//                                                        line[col++] = null; //"MentionSpeciale";
//                                                        line[col++] = rim.getCpostloc();
//                                                        line[col++] = rim.getCloc();
//
//                                                        line[col++] = null; //NomUe
//                                                        line[col++] = null; //PrenomUe
//                                                        line[col++] = null; //AnneeNaissanceUe
//
//                                                        line[col++] = sexe.toString();  // CSV : TYPE_QUEST
//                                                        line[col++] = sexe.toFullString(); //CSV : TYPE_QUEST
//                                                        line[col++] = String.valueOf(biDeLaRim.size()); //RPNBQUEST
//
//                                                        //Ensuite on écrit N fois la liste des prénoms
//                                                        //Suivis par d'éventuelles colonnes vides  (pour les familles avec moins de personnes concernées)
//                                                        //TODO: voir possibilité de mettre le anai en int directement au lieu de l'avoir en string
//                                                        //       ou mettre un bool indiquant qu'il est majeur ??
//                                                        String listPrenom =
//                                                            biEnquetes.stream().map(BIEntity::getPrenom).collect(Collectors.joining(", "));
//                                                        int i = 0;
//                                                        do {
//                                                                if (i < biDeLaRim.size()) {
//                                                                        line[col++] = listPrenom;
//                                                                }
//                                                                else {
//                                                                        line[col++] = "";
//                                                                }
//                                                                i++;
//                                                        }
//                                                        while (i < maxNbPersonnesMajeures);
//
//                                                        //Ensuite on traite chaque personne
//                                                        int compteur = 0;
//                                                        for (BIEntity bi : biDeLaRim) {
//                                                                //On ne garde que les majeurs du bon sexe
//                                                                if(!biEnquetes.contains(bi)){
//                                                                        continue;
//                                                                }
//
//                                                                compteur++;
//                                                                //Conjoint
//                                                                var conjointValue = conjointByIndividuID.get(bi.getId());
//                                                                if (conjointValue == null) {
//                                                                        //TODO : vide
//                                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";
//                                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";
//                                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";
//
//                                                                }
//                                                                else if (conjointValue.size() > 1) {
//                                                                        //TODO : vide
//                                                                        line[col++] = "PLUSIEURS CONJOINT";
//                                                                        line[col++] = "PLUSIEURS CONJOINT";
//                                                                        line[col++] = "PLUSIEURS CONJOINT";
//                                                                        log.error("L'individu [bi.id=" + bi.getId() + "] a plusieurs conjoints");
//                                                                }
//                                                                else {
//                                                                        BIEntity conjoint = individuByID.get(conjointValue.get(0));
//                                                                        line[col++] = "" + compteur + "--" + conjoint.getPrenom();
//                                                                        line[col++] = "" + compteur + "--" + conjoint.getSexe();
//                                                                        line[col++] = "" + compteur + "--" + conjoint.getAnai();
//                                                                }
//
//                                                                //Parents
//                                                                var parentsValue = parentByIndividuID.get(bi.getId());
//                                                                if (parentsValue == null) {
//                                                                        //TODO : vide
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                        line[col++] = null; //"" + compteur + "--" + "PAS_DE_PARENT";
//                                                                }
//                                                                else if (parentsValue.size() == 1) {
//                                                                        BIEntity parent1 = individuByID.get(parentsValue.get(0));
//                                                                        line[col++] = "" + compteur + "-parent-" + parent1.getPrenom();
//                                                                        line[col++] = "" + compteur + "-parent-" + parent1.getSexe();
//                                                                        line[col++] = "" + compteur + "-parent-" + parent1.getAnai();
//                                                                        line[col++] = null; //"";
//                                                                        line[col++] = null; //"";
//                                                                        line[col++] = null; //"";
//                                                                }
//                                                                else if (parentsValue.size() == 2) {
//                                                                        BIEntity parent1 = individuByID.get(parentsValue.get(0));
//                                                                        BIEntity parent2 = individuByID.get(parentsValue.get(1));
//                                                                        line[col++] = /*"" + compteur + "-parent1-" + */parent1.getPrenom();
//                                                                        line[col++] = /*"" + compteur + "-parent1-" +  */parent1.getSexe().toString();
//                                                                        line[col++] = /*"" + compteur + "-parent1-" +  */parent1.getAnai();
//                                                                        line[col++] = /*"" + compteur + "-parent2-" +  */parent2.getPrenom();
//                                                                        line[col++] = /*"" + compteur + "-parent2-" +  */parent2.getSexe().toString();
//                                                                        line[col++] = /*"" + compteur + "-parent2-" +  */parent2.getAnai();
//                                                                }
//                                                                else { //TODO : documenter
//                                                                        line[col++] = "PLUSIEURS PARENTS";
//                                                                        line[col++] = "PLUSIEURS PARENTS";
//                                                                        line[col++] = "PLUSIEURS PARENTS";
//                                                                        log.error("L'individu [bi.id=" + bi.getId() + "] a plus de deux parents");
//                                                                }
//
//                                                                //ENFANTS
//                                                                int compteurEnfant = 0;
//                                                                var enfantValue = enfantByIndividuID.get(bi.getId());
//                                                                if (enfantValue != null) {
//                                                                        for (Long enfantID : enfantValue) {
//                                                                                compteurEnfant++;
//                                                                                BIEntity enfant = individuByID.get(enfantID);
//                                                                                line[col++] =
//                                                                                    "" + compteur + "-enfant-" + compteurEnfant + "-" + enfant.getPrenom();
//                                                                                line[col++] =
//                                                                                    "" + compteur + "-enfant-" + compteurEnfant + "-" + enfant.getSexe();
//                                                                                line[col++] =
//                                                                                    "" + compteur + "-enfant-" + compteurEnfant + "-" + enfant.getAnai();
//                                                                        }
//                                                                }
//                                                                while (compteurEnfant < maxNbEnfant) {
//                                                                        compteurEnfant++;
//                                                                        line[col++] = null;
//                                                                        line[col++] = null;
//                                                                        line[col++] = null;
//                                                                }
//                                                        }
//                                                        writer.writeNext(line);
//                                                }
//                                                else {
//                                                        log.warn("Pas de BI pour idRim=" + rim.getId());
//                                                }
//                                        }
//                                        //On supprime celles-ci de la liste qui restera à traiter
//                                        rimsRestantes.subList(0, lastIndex).clear();
//                                }
//                        }
//                }
//                return file;
//        }
//
//
//        Map<Long, RIMEntity> extraireRIM2BDD (Date dateDebut, Date dateFin, Constantes.BI_SEXE sexe){
//                Map<Long, RIMEntity> toutesLesRIM = new HashMap<>();
//
//                //TODO: on a une méthode pour récupérer directement un stream
//                var rimOmer = omerDAO.getAllRIMForSexe(dateDebut, dateFin,sexe);
//                rimOmer.stream().forEach(rim -> toutesLesRIM.put(rim.getId(), rim));
//
//                var rimOdic = odicDAO.getAllRIMForSexe(dateDebut, dateFin, sexe);
//                rimOdic.forEach(rim -> {
//                        if (toutesLesRIM.containsKey(rim.getId())) {
//                                //TODO : doit on logger? Trop d'erreurs pénalisent les perfs
//                                //    log.warn("LA RIM id=" + rim.getId() + " existe à la fois dans HOMERE et dans ODIC");
//                        }
//                        else
//                                toutesLesRIM.put(rim.getId(), rim);
//                });
//                return toutesLesRIM;
//        }
//
//        void getBIEntityForRim(
//            List<Long> rimATraiter,
//            ArrayListValuedHashMap<Long, BIEntity> inoutBIByIDRIM,
//            Map<Long, BIEntity> inoutIndividuByID,
//            LinkedMultiValueMap<Long, Long> inoutConjointByIndividuID,
//            LinkedMultiValueMap<Long, Long> inoutParentByIndividuID,
//            LinkedMultiValueMap<Long, Long> inoutEnfantByIndividuID) {
//
//                // Récupération des FL et BI associés
//                List<BIEntity> listOfBi = new ArrayList<>();
//
//                //TODO : documenter que outListOfBI & lienByIndividuId sont inout (maj par les appels)
//                omerDAO.getBiEtLiensForRims(rimATraiter, listOfBi, inoutConjointByIndividuID, inoutParentByIndividuID, inoutEnfantByIndividuID);
//                odicDAO.getBiEtLiensForRims(rimATraiter, listOfBi, inoutConjointByIndividuID, inoutParentByIndividuID, inoutEnfantByIndividuID);
//
//                //TODO : déplacer ce code vers getBiEtLiensForRims et directement mettre à jour le résultat
//                for (var bi : listOfBi) {
//                        inoutBIByIDRIM.put(bi.getFeuillelogement(), bi);
//                        inoutIndividuByID.put(bi.getId(), bi);
//                }
//
//                return;
//        }
//
//        private Long getMaxNbEnfant () {
//                Long maxOmer = omerDAO.getMaxNbEnfant();
//                Long maxOdic = odicDAO.getMaxNbEnfant();
//                return (maxOdic > maxOdic) ? maxOmer : maxOdic;
//        }
//
//        private Long getMaxNbAdulte () {
//                Long maxOmer = omerDAO.getMaxNbAdulteMemeSexe();
//                Long maxOdic = odicDAO.getMaxNbAdulteMemeSexe();
//                return (maxOdic > maxOdic) ? maxOmer : maxOdic;
//        }
//
//        private static void updateHeader ( long maxNbEnfant, long maxNbPersonnesMajeures){
//
//                //ajout des RPLISTEPRENOMS_1...N au header
//                for (long i = 1; i <= maxNbPersonnesMajeures; i++) {
//                        HEADER_RECORD.add("RPLISTEPRENOMS_" + i);
//                }
//
//                //ajout des entêtes spécifiques
//                for (long i = 1; i <= maxNbPersonnesMajeures; i++) {
//                        //Conjoint
//                        HEADER_RECORD.add("RPPRENOMCONJ_" + i);
//                        HEADER_RECORD.add("RPSEXCONJ_" + i);
//                        HEADER_RECORD.add("RPANAISCONJ_" + i);
//
//                        //parent1
//                        HEADER_RECORD.add("RPPRENOMPAR1_" + i);
//                        HEADER_RECORD.add("RPSEXPAR1_" + i);
//                        HEADER_RECORD.add("RPANAISPAR1_" + i);
//
//                        //parent2
//                        HEADER_RECORD.add("RPPRENOMPAR2_" + i);
//                        HEADER_RECORD.add("RPSEXPAR2_" + i);
//                        HEADER_RECORD.add("RPANAISPAR2_" + i);
//
//                        //Enfants
//                        for (long j = 1; j <= maxNbEnfant; j++) {
//                                HEADER_RECORD.add("RPPRENOMENF" + j + "_" + i);
//                                HEADER_RECORD.add("RPSEXENF" + j + "_" + i);
//                                HEADER_RECORD.add("RPANAISENF" + j + "_" + i);
//                        }
//                }
//        }
//}
