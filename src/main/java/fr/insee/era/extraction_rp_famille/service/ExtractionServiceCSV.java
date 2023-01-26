package fr.insee.era.extraction_rp_famille.service;

import com.opencsv.CSVWriter;
import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.dao.OdicDAO;
import fr.insee.era.extraction_rp_famille.dao.OmerDAO;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.Constantes;
import fr.insee.era.extraction_rp_famille.model.dto.RIMDto;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.model.exception.CommuneInconnueException;
import fr.insee.era.extraction_rp_famille.model.exception.ConfigurationException;
import fr.insee.era.extraction_rp_famille.model.exception.RimInconnueException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j @Service public class ExtractionServiceCSV {

        List<String> HEADER_RECORD = new ArrayList<>(
            Arrays.asList("Identifiant", "IdModele", "IdeC" , "IdLot", "CiviliteReferent", "NomReferent", "PrenomReferent", "MailReferent", "NumeroVoie",
                "IndiceRepetition", "TypeVoie", "LibelleVoie", "ComplementAdresse", "MentionSpeciale", "CodePostal", "LibelleCommune", "NomUe", "PrenomUe",
                "AnneeNaissanceUe", "TYPE_QUEST", "RPTYPEQUEST", "RPNBQUEST", "whoAnswers1", "whoAnswers2", "whoAnswers3"));
        @Autowired OmerDAO omerDAO;
        @Autowired OdicDAO odicDAO;

        //Super moche mais évite de refactorer juste pour cette sortie CSV ou de dupliquer du code
        @Autowired ExtractionServiceJSON extractionServiceJSON;
        @Autowired ParametrageConfiguration parametrageProperties;

        ExtractionServiceCSV(){
                //On doit adapter le header au nombre max de personnes et au nb max d'enfants
                updateHeader();
        }
        public ByteArrayOutputStream getAllRimForPeriodAsCSV(Date dateDebut, Date dateFin, Constantes.BI_SEXE sexe, String questionnaireId)
            throws RimInconnueException, CommuneInconnueException, IOException, ConfigurationException {
                log.info("Extraction CSV entre dateDebut={} et dateFin={}",dateDebut,dateFin);

                ByteArrayOutputStream csvResultOutputStream= new ByteArrayOutputStream();

                //Récupération de la liste des UE à traiter
                //Malheureusment, avec la suppression des BI dans OMER et leur déplacement dans ODIC
                //on n'a pas pu filtrer ces RIM sur le sexe, l'age et le tableauabcd
                Collection<ReponseListeUEDto> toutesLesRIM = extractionServiceJSON.getAllRimForPeriod(dateDebut,dateFin,sexe);

                //premier filtres : mail
                toutesLesRIM.removeIf(reponseListeUEDto -> !(estValide(reponseListeUEDto)));

                int nbRimTraitées=0;
                try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(csvResultOutputStream))) {
                        //Ecriture du header CSV
                        writer.writeNext(HEADER_RECORD.toArray(new String[HEADER_RECORD.size()]));

                        //Traitement de chaque rim individuellement
                        for(ReponseListeUEDto rim : toutesLesRIM){
                                nbRimTraitées++;
                                if(  nbRimTraitées % 500 == 1) {
                                        log.info("traitement de la RIM {} / {} ", nbRimTraitées, toutesLesRIM.size());
                                }
                                //Récupération des détails de la RIM
                                HashMap<Long, BIEntity> biEntityById = new HashMap<>();
                                HashMap<Long, Long> conjointByIndividuID = new HashMap<>();
                                LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId = new LinkedMultiValueMap<>();
                                LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId = new LinkedMultiValueMap<>();

                                List<BIEntity> biomer = omerDAO.getBiEtLiensForRim(rim.getId(),conjointByIndividuID,inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);
                                List<BIEntity> biodic = odicDAO.getBiEtLiensForRim(rim.getId(),conjointByIndividuID,inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);

                                for (BIEntity bi : biomer) {
                                        biEntityById.put(bi.getId(),bi);
                                }
                                for (BIEntity bi : biodic) {
                                        if(biEntityById.containsKey(bi.getId())){
                                                log.debug(String.format("bi[id=%s] existe dans OMER et ODIC; on garde la valeur OMER",bi.getId()));
                                        }
                                        else {
                                                biEntityById.put(bi.getId(), bi);
                                        }
                                }


                                var rimDetails  = omerDAO.getRim(rim.getId());
                                if( rimDetails== null){
                                        rimDetails=odicDAO.getRim(rim.getId());
                                }
                                if(rimDetails==null){
                                        throw new RimInconnueException(rim.getId());
                                }

                                if (!biEntityById.isEmpty()) {

                                        //Déjà as on un pax majeur du bon sexe???
                                        List<BIEntity> biEnquetes =
                                            biEntityById.values().stream().filter(
                                                    biEntity ->
                                                        ( Integer.valueOf(biEntity.getAnai()) <= Constantes.ANNEE_NAISSANCE_MAJEUR
                                                            && biEntity.getSexe()==sexe
                                                        ))
                                                //On ne garde que le nombre max de personnes à enquêter
                                                .limit(Constantes.NB_MAX_PERSONNES_ENQUETEES)
                                                .collect(Collectors.toList());

                                        if(biEnquetes.isEmpty()){
                                                log.warn("BIid={} Pas de personne majeure de sexe={}",rim.getId(),sexe);
                                                continue;
                                        }


                                        String[] line = new String[HEADER_RECORD.size()];

                                        //Une seule ligne par logement
                                        int col = 0;
                                        line[col++] = String.valueOf(rim.getId()); //"Identifiant";
                                        line[col++] = questionnaireId; //"IdModele";
                                        line[col++] = rim.getInternaute(); //"IdeC (internaute ; identifiant internet...)
                                        line[col++] = sexe.toString(); //"IDLot";
                                        line[col++] = null; //"CiviliteReferent";
                                        line[col++] = null; //"NomReferent";
                                        line[col++] = null; //"PrenomReferent";
                                        line[col++] = rim.getMail(); //"MailReferent";
                                        line[col++] = null;rimDetails.getNumvoiloc();
                                        line[col++] = null; //"IndiceRepetition";
                                        line[col++] = null;//rimDetails.getTypevoiloc();
                                        //HACK : on vient mettre l'adresse complète dans le champ libellé voie
                                        //line[col++] = rimDetails.getNomvoiloc();
                                        line[col++] = calculerAdresseSansCommune(rimDetails);

                                        line[col++] = null; //"ComplementAdresse";
                                        line[col++] = null; //"MentionSpeciale";
                                        line[col++] = rimDetails.getCpostloc();
                                        line[col++] = rimDetails.getCloc();

                                        line[col++] = null; //NomUe
                                        line[col++] = null; //PrenomUe
                                        line[col++] = null; //AnneeNaissanceUe

                                        line[col++] = sexe.toString();  // CSV : TYPE_QUEST
                                        line[col++] = sexe.toFullString(); //CSV : TYPE_QUEST
                                        line[col++] = String.valueOf(biEnquetes.size()); //RPNBQUEST

                                        //Whoanswers
                                        if(Constantes.BI_SEXE.BI_SEXE_FEMME.equals(sexe)){
                                                line[col++] = "Dans votre foyer, chaque femme âgée de 18 ans ou plus doit répondre : "; //whoAnswers1
                                        }
                                        else{
                                                line[col++] = "Dans votre foyer, chaque homme âgé de 18 ans ou plus doit répondre : "; //whoAnswers1
                                        }

                                        String listePrenom = String.join(", ",biEnquetes.stream().map(BIEntity::getPrenom).collect(Collectors.toList()));
                                        line[col++] = listePrenom; //whoAnswers2
                                        line[col++] = ""; //whoAnswers3

                                        //Ensuite on écrit N fois la liste des prénoms
                                        //Suivis par d'éventuelles colonnes vides  (pour les familles avec moins de personnes concernées)
                                        int i = 0;
                                        do {
                                                if (i < biEnquetes.size()) {
                                                        line[col++] = listePrenom;
                                                }
                                                else {
                                                        line[col++] = "";
                                                }
                                                i++;
                                        }
                                        while (i < Constantes.NB_MAX_PERSONNES_ENQUETEES);

                                        //Ensuite on traite chaque personne
                                        int compteur = 0;
                                        for (BIEntity bi : biEntityById.values()) {
                                                //On ne garde que les majeurs du bon sexe
                                                if(!biEnquetes.contains(bi)){
                                                        continue;
                                                }

                                                compteur++;
                                                //Conjoint
                                                var conjointId = conjointByIndividuID.get(bi.getId());
                                                if (conjointId == null) {
                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";
                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";
                                                        line[col++] = null; //"" + compteur + "PAS_DE_CONJOINT";

                                                }
                                                else {
                                                        BIEntity conjoint = biEntityById.get(conjointId);
                                                        line[col++] = conjoint.getPrenom();
                                                        line[col++] = conjoint.getSexe().toString();
                                                        line[col++] = conjoint.getAnai();
                                                }

                                                //Parents
                                                //parents de l'individu (triés du plus vieux au plus jeune : règle métier)
                                                List<Long> parents = inoutLienEnfantByIndividuId.get(bi.getId());
                                                BIEntity parent1=null;
                                                BIEntity parent2 = null;

                                                if(parents!=null){
                                                        if(parents.size()>=2){
                                                                BIEntity parentA = biEntityById.get(parents.get(0));
                                                                BIEntity parentB = biEntityById.get(parents.get(1));
                                                                //Parent A est né avant parent 2 (une année plus "petite")
                                                                //On l'écrit donc en 1er
                                                                if(Integer.valueOf(parentA.getAnai())<Integer.valueOf(parentB.getAnai())){
                                                                        parent1 = parentA;
                                                                        parent2= parentB;
                                                                }
                                                                else{
                                                                        parent1=parentB;
                                                                        parent2=parentA;
                                                                }
                                                        }
                                                        else if(parents.size()==1){
                                                                parent1 = biEntityById.get(parents.get(0));
                                                        }
                                                }

                                                if (parent1 == null) {
                                                        line[col++] = null;
                                                        line[col++] = null;
                                                        line[col++] = null;
                                                        line[col++] = null;
                                                        line[col++] = null;
                                                        line[col++] = null;
                                                }
                                                else{
                                                        line[col++] = parent1.getPrenom();
                                                        line[col++] = parent1.getSexe().toString();
                                                        line[col++] = parent1.getAnai();
                                                        if(parent2==null){
                                                                line[col++] = null;
                                                                line[col++] = null;
                                                                line[col++] = null;
                                                        }
                                                        else{
                                                                line[col++] = parent2.getPrenom();
                                                                line[col++] = parent2.getSexe().toString();
                                                                line[col++] = parent2.getAnai();
                                                        }
                                                }

                                                //ENFANTS
                                                var enfants = inoutLienParentByIndividuId.get(bi.getId());
                                                int nbEnfants=(enfants==null)?0:enfants.size();
                                                for (int j=0; j< Constantes.NB_MAX_ENFANT_PAR_PERSONNE; j++){
                                                        //TODO: faire un writer pour une entité de 3 champs comme pour le json

                                                        if(j<nbEnfants){
                                                                BIEntity enfantEntity = biEntityById.get(enfants.get(j));
                                                                if(enfantEntity==null)
                                                                {
                                                                        int ttt=0;
                                                                        ttt++;
                                                                }
                                                                line[col++] = enfantEntity.getPrenom();
                                                                line[col++] = enfantEntity.getSexe().toString();
                                                                line[col++] = enfantEntity.getAnai();
                                                        }
                                                        else{
                                                                line[col++] = null;
                                                                line[col++] = null;
                                                                line[col++] = null;
                                                        }
                                                }
                                        }
                                        writer.writeNext(line);
                                }
                                else {
                                        log.warn("Pas de BI pour idRim=" + rim.getId());
                                }
                        }
                        log.info("FIN  {} / {} ", nbRimTraitées, toutesLesRIM.size());

                }

                return csvResultOutputStream;
        }

        /**
         * Validation de certain champs avec les REGEXP de MIT
         * @param responseListeUe
         * @return
         */
        private static boolean estValide(ReponseListeUEDto responseListeUe) {
                boolean estValide=false;

                //Courriel
                if(  responseListeUe.getMail().length()<=80
                        &&
                    responseListeUe.getMail().matches("^[a-zA-Z0-9_]+([.-]{1}[a-zA-Z0-9_]+)*@[a-zA-Z0-9]+([.-]{1}[a-zA-Z0-9]+)*.[a-zA-Z]{2,}$")
                )
                {
                        estValide=true;
                }
                else{
                        log.info("Suppression de la rim avec courriel={}",responseListeUe.getMail());
                        estValide=false;
                }
                return estValide;
        }

        private void updateHeader (){

                //ajout des RPLISTEPRENOMS_1...N au header
                for (long i = 0; i < Constantes.NB_MAX_PERSONNES_ENQUETEES; i++) {
                        HEADER_RECORD.add("RPLISTEPRENOMS_" + i);
                }

                //ajout des entêtes spécifiques
                for (long i = 0; i < Constantes.NB_MAX_PERSONNES_ENQUETEES; i++) {
                        //Conjoint
                        HEADER_RECORD.add("RPPRENOMCONJ_" + i);
                        HEADER_RECORD.add("RPSEXCONJ_" + i);
                        HEADER_RECORD.add("RPANAISCONJ_" + i);

                        //parent1
                        HEADER_RECORD.add("RPPRENOMPAR1_" + i);
                        HEADER_RECORD.add("RPSEXPAR1_" + i);
                        HEADER_RECORD.add("RPANAISPAR1_" + i);

                        //parent2
                        HEADER_RECORD.add("RPPRENOMPAR2_" + i);
                        HEADER_RECORD.add("RPSEXPAR2_" + i);
                        HEADER_RECORD.add("RPANAISPAR2_" + i);

                        //Enfants
                        for (long j = 1; j <= Constantes.NB_MAX_ENFANT_PAR_PERSONNE; j++) {
                                HEADER_RECORD.add("RPPRENOMENF" + j + "_" + i);
                                HEADER_RECORD.add("RPSEXENF" + j + "_" + i);
                                HEADER_RECORD.add("RPANAISENF" + j + "_" + i);
                        }
                }
                log.info("header={}",HEADER_RECORD);
        }

        public static String calculerAdresseSansCommune(RIMDto rim){
                 String adresse =  String.format("%s %s %s",
                        rim.getNumvoiloc(),
                        rim.getTypevoiloc(),
                        rim.getNomvoiloc()
                    ).replaceAll("\\p{Zs}+", " ");
                 //TODO : constante?
                int maxSize=38;
                 if(adresse.length()>maxSize) {
                         log.info("rim identifiantInternet={} : champ adresse trop long coupé à {} caractères adresseOriginale={}",
                         rim.getIdentifiantInternet(),maxSize, adresse);
                         adresse=adresse.substring(0,maxSize-1);
                }
                 return adresse;
        }
}
