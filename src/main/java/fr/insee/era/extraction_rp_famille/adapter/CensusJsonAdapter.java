package fr.insee.era.extraction_rp_famille.adapter;

import fr.insee.era.extraction_rp_famille.model.dto.*;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.model.enums.RelationshipType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CensusJsonAdapter {

    public CensusJsonDto convert(ResponseNetUserDto responseNetUserDto, GenderType gender) {
        return CensusJsonDto.builder()
                .id(responseNetUserDto.getId())
                .numvoiloc(responseNetUserDto.getNumvoiloc())
                .bisterloc(responseNetUserDto.getBisterloc())
                .typevoiloc(responseNetUserDto.getTypevoiloc())
                .nomvoiloc(responseNetUserDto.getNomvoiloc())
                .resloc(responseNetUserDto.getResloc())
                .car(responseNetUserDto.getCar())
                .cpostloc(responseNetUserDto.getCpostloc())
                .idinternaute(responseNetUserDto.getIdinternaute())
                .mail(responseNetUserDto.getMail())
                .identifiantCompte(responseNetUserDto.getIdentifiant())
                .externals(convertExternals(responseNetUserDto, gender))
                .build();
    }

    private ExternalJsonDto convertExternals(ResponseNetUserDto responseNetUserDto, GenderType gender) {
        // list to keep order
        List<Long> surveyedIds = responseNetUserDto.getIndividuals().stream().filter(i -> i.isMajor() && i.isSurveyed())
                .map(IndividualFormDto::getId).toList();
        // Map individuals by id
        Map<Long, IndividualFormDto> individualsMap = responseNetUserDto.getIndividuals().stream()
                .collect(Collectors.toMap(IndividualFormDto::getId, i -> i));

        // Get questionnaire variables
        String rpListePrenoms = surveyedIds.stream().map(individualsMap::get)
                .map(IndividualFormDto::getFirstName).collect(Collectors.joining(", "));
        SimpleVariablesDto simpleVariablesDto = SimpleVariablesDto.builder()
                .typeQuest(gender.getValue())
                .rpTypeQuest(gender.getLabel())
                .rpNbQuest(Long.toString(surveyedIds.size()))
                .rpListePrenoms(rpListePrenoms)
                .build();



        Map<Long, LoopVariablesDto> surveyedVariablesMap = new HashMap<>();
        Map<Long, LoopVariablesDto> conjointVariablesMap = new HashMap<>();
        Map<Long, List<LoopVariablesDto>> parentsVariablesMap = new HashMap<>();
        Map<Long, List<LoopVariablesDto>> enfantsVariablesMap = new HashMap<>();
        for (long surveyedId : surveyedIds) {
            // Get all surveyed variables
            IndividualFormDto individual = individualsMap.get(surveyedId);
            LoopVariablesDto surveyedVariables = fillLoopVariables(individual, RelationshipType.ENQUETE, null);
            surveyedVariablesMap.put(surveyedId, surveyedVariables);

            // Families Ties
            List<RelationshipDto> familyTies = individual.getFamilyTies();

            // Get conjoint
            Optional<Long> idConj = familyTies.stream().filter(f -> f.getRelationshipType().equals(RelationshipType.CONJOINT))
                    .map(RelationshipDto::getIdRelationship)
                    .findFirst();
            Optional<IndividualFormDto> conjoint = Optional.empty();
            if (idConj.isPresent()) {
                conjoint = individualsMap.values().stream().filter(i -> i.getId() == idConj.get()).findFirst();
            }
            conjoint.ifPresent(c -> conjointVariablesMap.put(surveyedId, fillLoopVariables(c, RelationshipType.CONJOINT, null)));

            // Get parents
            List<Long> idParents = familyTies.stream().filter(f -> f.getRelationshipType().equals(RelationshipType.ENFANT))
                    .map(RelationshipDto::getIdRelationship).toList();
            List<IndividualFormDto> parents = individualsMap.values().stream()
                    .filter(i -> idParents.contains(i.getId())).toList();
            int nbParents = Collections.min(List.of(2, parents.size()));
            List<LoopVariablesDto> parentsIndividuals = new ArrayList<>();
            for (int i = 0; i < nbParents; i++) {
                LoopVariablesDto parentVariables = fillLoopVariables(parents.get(i), RelationshipType.PARENT, i + 1);
                parentsIndividuals.add(parentVariables);
            }
            parentsVariablesMap.put(surveyedId, parentsIndividuals);

            // Get enfants
            List<Long> idEnfants = familyTies.stream().filter(f -> f.getRelationshipType().equals(RelationshipType.PARENT))
                    .map(RelationshipDto::getIdRelationship).toList();
            List<IndividualFormDto> enfants = individualsMap.values().stream()
                    .filter(i -> idEnfants.contains(i.getId())).toList();
            int nbEnfants = Collections.min(List.of(12, enfants.size()));
            List<LoopVariablesDto> enfantsIndividuals = new ArrayList<>();
            for (int i = 0; i < nbEnfants; i++) {
                LoopVariablesDto enfantVariables = fillLoopVariables(enfants.get(i), RelationshipType.ENFANT, i + 1);
                parentsIndividuals.add(enfantVariables);
            }
            enfantsVariablesMap.put(surveyedId, enfantsIndividuals);
        }

        return ExternalJsonDto.builder()
                .surveyedIds(surveyedIds)
                .simpleVariableDto(simpleVariablesDto)
                .surveyedVariablesList(surveyedVariablesMap)
                .conjointVariablesList(conjointVariablesMap)
                .parentsVariablesList(parentsVariablesMap)
                .enfantsVariablesList(enfantsVariablesMap)
                .build();
    }

    private LoopVariablesDto fillLoopVariables(IndividualFormDto individual, RelationshipType relationshipType, Integer order) {
        return LoopVariablesDto.builder()
                .type(relationshipType)
                .id(String.valueOf(individual.getId()))
                .index(order)
                .firstName(individual.getFirstName())
                .gender(individual.getGender())
                .birthYear(individual.getBirthYear())
                .birthMonth(individual.getBirthMonth())
                .birthDay(individual.getBirthDay())
                .dpnaicode(individual.getDpnaicode())
                .cnaif(individual.getCnaif())
                .cnaie(individual.getCnaie())
                .pnai(individual.getPnai())
                .build();
    }
}
