package fr.insee.era.extraction_rp_famille.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.era.extraction_rp_famille.configuration.ParametrageConfiguration;
import fr.insee.era.extraction_rp_famille.dao.OdicDAO;
import fr.insee.era.extraction_rp_famille.dao.OmerDAO;
import fr.insee.era.extraction_rp_famille.model.BIEntity;
import fr.insee.era.extraction_rp_famille.model.Constantes;
import fr.insee.era.extraction_rp_famille.model.dto.ReponseListeUEDto;
import fr.insee.era.extraction_rp_famille.model.exception.CommuneInconnueException;
import fr.insee.era.extraction_rp_famille.model.exception.PasDeBIDuBonSexeException;
import fr.insee.era.extraction_rp_famille.model.exception.RimInconnueException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j @Service public class ExtractionServiceJSON {

        @Autowired OmerDAO omerDAO;
        @Autowired OdicDAO odicDAO;
        @Autowired ObjectMapper jacksonObjectMapper;

        @Autowired ParametrageConfiguration parametrageProperties;


        public Collection<ReponseListeUEDto> getAllRimForPeriod(Date dateDebut, Date dateFin) throws DataAccessException {

                HashMap<Long, ReponseListeUEDto> reponseByIdRim = new HashMap<>();

                //TODO: on a une méthode pour récupérer directement un stream
                var listeOMER = omerDAO.getIdRIMetInternetForPeriod(dateDebut, dateFin);
                listeOMER.stream().forEach(pair -> reponseByIdRim.put(pair.getLeft(),new ReponseListeUEDto(pair.getLeft(),pair.getRight())));

                var listeODIC = odicDAO.getIdRIMetInternetForPeriod(dateDebut, dateFin);
                listeODIC.forEach(pair -> {
                        if (reponseByIdRim.containsKey(pair.getLeft())) {
                                //TODO : doit on logger? Trop d'erreurs pénalisent les perfs
                                //    log.warn("LA RIM id=" + rim.getId() + " existe à la fois dans HOMERE et dans ODIC");
                        }
                        else
                                reponseByIdRim.put(pair.getLeft(),new ReponseListeUEDto(pair.getLeft(),pair.getRight()));
                });

                return reponseByIdRim.values();
        }

        public ObjectNode getDataForRim(Long idRim, String questionnaireId) throws Exception {

                HashMap<Long, BIEntity> biEntityById = new HashMap<>();
                HashMap<Long, Long> conjointByIndividuID = new HashMap<>();
                LinkedMultiValueMap<Long, Long> inoutLienParentByIndividuId = new LinkedMultiValueMap<>();
                LinkedMultiValueMap<Long, Long> inoutLienEnfantByIndividuId = new LinkedMultiValueMap<>();

                List<BIEntity> biomer = omerDAO.getBiEtLiensForRim(idRim,conjointByIndividuID,inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);
                List<BIEntity> biodic = odicDAO.getBiEtLiensForRim(idRim,conjointByIndividuID,inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);

                for (var bi : biomer) {
                        biEntityById.put(bi.getId(),bi);
                }
                for (var bi : biodic) {
                        if(biEntityById.containsKey(bi.getId())){
                                log.debug(String.format("bi[id=%s] existe dans OMER et ODIC; on garde la valeur OMER",bi.getId()));
                        }
                        else {
                                biEntityById.put(bi.getId(), bi);
                        }
                }


                var rimDetails  = omerDAO.getRim(idRim);
                if( rimDetails== null){
                        rimDetails=odicDAO.getRim(idRim);
                }
                if(rimDetails==null){
                        throw new RimInconnueException(idRim);
                }
                String rimAdresse = rimDetails.getLeft();
                String rimIdentifiantInternet = rimDetails.getMiddle();
                String identifiantCommune = rimDetails.getRight();

                Constantes.BI_SEXE sexe;
                if(parametrageProperties.getCommunesFemmes().contains(identifiantCommune)){
                        sexe=Constantes.BI_SEXE.BI_SEXE_FEMME;
                }
                else if(parametrageProperties.getCommunesHommes().contains(identifiantCommune)){
                        sexe=Constantes.BI_SEXE.BI_SEXE_HOMME;
                }
                else{
                        throw new CommuneInconnueException(identifiantCommune);
                }



                ObjectNode resultat  = jacksonObjectMapper.createObjectNode();

                //Json pour Coleman questionnaire
                ObjectNode jsonQuestionnaire=computeColemanQuestionnaireReponse(idRim,sexe,biEntityById,conjointByIndividuID,inoutLienParentByIndividuId,inoutLienEnfantByIndividuId);
                jsonQuestionnaire.put("questionnaireId", questionnaireId );
                jsonQuestionnaire.set("personalization",jacksonObjectMapper.createObjectNode() );
                jsonQuestionnaire.set("comment", jacksonObjectMapper.createObjectNode());
                jsonQuestionnaire.set("stateData", jacksonObjectMapper.createObjectNode());

                //Json pour coleman pilotage
                ObjectNode jsonPilotage=computeColemanPilotageReponse(idRim,rimAdresse,rimIdentifiantInternet,questionnaireId);

                resultat.set("questionnaire",jsonQuestionnaire);
                resultat.set("pilotage",jsonPilotage);

                return resultat;
        }

        private ObjectNode computeColemanPilotageReponse(Long idRim, String adresse, String identifiantInternet, String questionnaireId) {
                ObjectNode resultat  = jacksonObjectMapper.createObjectNode();
                resultat.put("address", adresse);
                resultat.put("batchNumber", 1);
                resultat.put("firstname", "");
                resultat.put("lastname", "");
                resultat.put("idCampaign", questionnaireId);
                resultat.put("idContact", identifiantInternet);
                resultat.put("idSu", String.valueOf(idRim));
                return resultat;
        }

        private ObjectNode computeColemanQuestionnaireReponse(
            Long idRim,
            Constantes.BI_SEXE sexe, HashMap<Long, BIEntity> biEntityById,
            HashMap<Long, Long> conjointByIndividuID,
            LinkedMultiValueMap<Long, Long> lienParentByIndividuId,
            LinkedMultiValueMap<Long, Long> lienEnfantByIndividuId) throws Exception {

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
                        log.warn("Pas de personne majeure de sexe== "+sexe);
                        throw new PasDeBIDuBonSexeException(idRim,sexe);
                }



                ObjectNode objectNode = jacksonObjectMapper.createObjectNode();
                ObjectNode dataNode = jacksonObjectMapper.createObjectNode();
                objectNode.put("id",idRim);

                objectNode.set("data",dataNode);
                ObjectNode externalNode = jacksonObjectMapper.createObjectNode();
                dataNode.set("EXTERNAL",externalNode);
                externalNode.put("RPNBQUEST",biEnquetes.size());
                externalNode.put("RPTYPEQUEST",sexe.toFullString());

                ArrayNode listePrenomNode = jacksonObjectMapper.createArrayNode();
                String listePrenom = String.join(", ",biEnquetes.stream().map(BIEntity::getPrenom).collect(Collectors.toList()));
                for (int i = 0; i<biEnquetes.size(); i++){
                        listePrenomNode.add(listePrenom);
                }

                externalNode.set("RPLISTEPRENOMS",listePrenomNode);

                ArrayNode prenomConjointNode = jacksonObjectMapper.createArrayNode();
                ArrayNode sexeConjointNode = jacksonObjectMapper.createArrayNode();
                ArrayNode anaisConjointNode = jacksonObjectMapper.createArrayNode();


                ArrayNode prenomPar1Node = jacksonObjectMapper.createArrayNode();
                ArrayNode sexePar1Node = jacksonObjectMapper.createArrayNode();
                ArrayNode anaisPar1Node = jacksonObjectMapper.createArrayNode();

                ArrayNode prenomPar2Node = jacksonObjectMapper.createArrayNode();
                ArrayNode sexePar2Node = jacksonObjectMapper.createArrayNode();
                ArrayNode anaisPar2Node = jacksonObjectMapper.createArrayNode();



                ArrayNode[][] nodesEnfants = new ArrayNode[Constantes.NB_MAX_ENFANT_PAR_PERSONNE][3] ;
                int posPrenom=0;
                int posSexe=1;
                int posAnai=2;
                for(int j=0; j<Constantes.NB_MAX_ENFANT_PAR_PERSONNE; j++) {
                        nodesEnfants[j][posPrenom] = jacksonObjectMapper.createArrayNode();
                        nodesEnfants[j][posSexe] = jacksonObjectMapper.createArrayNode();
                        nodesEnfants[j][posAnai] = jacksonObjectMapper.createArrayNode();
                        //On met des +1 car les indices commencent à 1 et non pas à 0
                        externalNode.set("RPPRENOMENF"+(j+1),nodesEnfants[j][posPrenom]);
                        externalNode.set("RPSEXENF"+(j+1),nodesEnfants[j][posSexe]);
                        externalNode.set("RPANAISENF"+(j+1),nodesEnfants[j][posAnai]);
                }

                for (int i = 0; i<biEnquetes.size(); i++){
                        Long biId = biEnquetes.get(i).getId();
                        Long conjointId = conjointByIndividuID.get(biId);
                        BIEntity conjointEntity = biEntityById.get(conjointId);
                        //Conjoint
                        initJsonNodePrenomSexeAnai(conjointEntity,prenomConjointNode,sexeConjointNode,anaisConjointNode);

                        //parents de l'individu (triés du plus vieux au plus jeune : règle métier)
                        //TODO : mettre un TU sur le tri
                        List<Long> parents = lienEnfantByIndividuId.get(biId);
                        BIEntity parent1=null, parent2 = null;

                        if(parents!=null){
                                if(parents.size()>=2){
                                        BIEntity parentA = biEntityById.get(parents.get(0));
                                        BIEntity parentB = biEntityById.get(parents.get(1));
                                        if(Integer.valueOf(parentA.getAnai())>Integer.valueOf(parentB.getAnai())){
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

                        //parent1
                        initJsonNodePrenomSexeAnai(parent1,prenomPar1Node,sexePar1Node,anaisPar1Node);

                        //parent2
                        initJsonNodePrenomSexeAnai(parent2,prenomPar2Node,sexePar2Node,anaisPar2Node);

                        //Enfants
                        var enfants = lienParentByIndividuId.get(biId);
                        int nbEnfants=(enfants==null)?0:enfants.size();
                        for (int j=0; j< Constantes.NB_MAX_ENFANT_PAR_PERSONNE; j++){
                                BIEntity enfantEntity = (j<nbEnfants)?biEntityById.get(enfants.get(j)):null;
                                initJsonNodePrenomSexeAnai(enfantEntity,nodesEnfants[j][posPrenom],nodesEnfants[j][posSexe],nodesEnfants[j][posAnai]);
                        }
                }


                externalNode.set("RPPRENOMCONJ",prenomConjointNode);
                externalNode.set("RPSEXCONJ",sexeConjointNode);
                externalNode.set("RPANAISCONJ",anaisConjointNode);

                externalNode.set("RPPRENOMPAR1",prenomPar1Node);
                externalNode.set("RPSEXPAR1",sexePar1Node);
                externalNode.set("RPANAISPAR1",anaisPar1Node);

                externalNode.set("RPPRENOMPAR2",prenomPar2Node);
                externalNode.set("RPSEXPAR2",sexePar2Node);
                externalNode.set("RPANAISPAR2",anaisPar2Node);


                return objectNode;
        }

        private void initJsonNodePrenomSexeAnai(BIEntity source, ArrayNode prenomNode, ArrayNode sexeNode, ArrayNode anaiNode ){
                if(source!=null) {
                        prenomNode.add(source.getPrenom());
                        sexeNode.add(source.getSexe().toString());
                        anaiNode.add(source.getAnai());
                }
                else{
                        prenomNode.add(jacksonObjectMapper.nullNode());
                        sexeNode.add(jacksonObjectMapper.nullNode());
                        anaiNode.add(jacksonObjectMapper.nullNode());
                }
        }
}
