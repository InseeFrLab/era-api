package fr.insee.era.extraction_rp_famille.adapter;

import fr.insee.era.extraction_rp_famille.model.BusinessConstant;
import fr.insee.era.extraction_rp_famille.model.dto.*;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class CensusJsonAdapter {

    public CensusSurveyUnitDto convert(ResponseNetUserDto responseNetUserDto, GenderType gender) {
        return CensusSurveyUnitDto.builder()
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

    private ExternalsVariables convertExternals(ResponseNetUserDto responseNetUserDto, GenderType gender) {

        // List of Surveyed and major
        List<IndividualFormDto> listOfSurveyedMajor = responseNetUserDto.getListOfSurveyedMajor();

        // Simples Variables
        SimpleVariable typeQuest = SimpleVariable.builder().name("TYPE_QUEST").value(gender.getValue()).build();
        SimpleVariable rpTypeQuest = SimpleVariable.builder().name("RPTYPEQUEST").value(gender.getLabel()).build();
        SimpleVariable rpNbQuest = SimpleVariable.builder().name("RPNBQUEST").value(Integer.toString(listOfSurveyedMajor.size())).build();
        List<SimpleVariable> simpleVariables = List.of(typeQuest, rpTypeQuest, rpNbQuest);

        // Loop Variables
        LoopVariable rpListePrenoms = LoopVariable.builder().name("RPLISTEPRENOMS").values(new ArrayList<>()).build();

        Map<Integer, LoopVariable> loopVariableMapFirstNameParent = new HashMap<>();
        Map<Integer, LoopVariable> loopVariableMapBirthYearParent = new HashMap<>();
        Map<Integer, LoopVariable> loopVariableMapGenderParent = new HashMap<>();
        for (int i = 1; i<=2 ; i++) {
            loopVariableMapFirstNameParent.put(i, LoopVariable.builder().name("RPPRENOMPAR" + i).values(new ArrayList<>()).build());
            loopVariableMapBirthYearParent.put(i, LoopVariable.builder().name("RPANAISPAR" + i).values(new ArrayList<>()).build());
            loopVariableMapGenderParent.put(i, LoopVariable.builder().name("RPSEXPAR" + i).values(new ArrayList<>()).build());
        }

        LoopVariable rpPrenomConj = LoopVariable.builder().name("RPPRENOMCONJ").values(new ArrayList<>()).build();
        LoopVariable rpAnaisConj = LoopVariable.builder().name("RPANAISCONJ").values(new ArrayList<>()).build();
        LoopVariable rpSexConj = LoopVariable.builder().name("RPSEXCONJ").values(new ArrayList<>()).build();

        Map<Integer, LoopVariable> loopVariableMapFirstNameChildren = new HashMap<>();
        Map<Integer, LoopVariable> loopVariableMapBirthYearChildren = new HashMap<>();
        for (int i = 1; i <= BusinessConstant.MAX_CHILDREN_PER_PERSON; i++) {
            loopVariableMapFirstNameChildren.put(i, LoopVariable.builder().name("RPPRENOMENF" + i).values(new ArrayList<>()).build());
            loopVariableMapBirthYearChildren.put(i, LoopVariable.builder().name("RPANAISENF" + i).values(new ArrayList<>()).build());
        }

        String firstNameList = listOfSurveyedMajor.stream().map(IndividualFormDto::getFirstName).collect(Collectors.joining(", "));
        for (IndividualFormDto individual : listOfSurveyedMajor) {
            rpListePrenoms.getValues().add(firstNameList);
            // Get conjoint
            IndividualFormDto conjoint = responseNetUserDto.getConjointByIndividual(individual);
            rpPrenomConj.getValues().add(conjoint.getFirstName());
            rpAnaisConj.getValues().add(conjoint.getBirthYear());
            rpSexConj.getValues().add(conjoint.getGender());

            // Get parents
            Map<Integer,IndividualFormDto> parents = responseNetUserDto.getListOfParentsByIndividual(individual);
            for (int i = 1; i <= 2; i++) {
                loopVariableMapFirstNameParent.get(i).getValues().add(parents.get(i).getFirstName());
                loopVariableMapBirthYearParent.get(i).getValues().add(parents.get(i).getBirthYear());
                loopVariableMapGenderParent.get(i).getValues().add(parents.get(i).getGender());
            }

            // Get children
            Map<Integer,IndividualFormDto> children = responseNetUserDto.getListOfChildrenByIndividual(individual);
            for (int i = 1; i <= BusinessConstant.MAX_SURVEYED_PERSONS; i++) {
                loopVariableMapFirstNameChildren.get(i).getValues().add(children.get(i).getFirstName());
                loopVariableMapBirthYearChildren.get(i).getValues().add(children.get(i).getBirthYear());
            }
        }
        List<LoopVariable> loopVariables = new ArrayList<>();
        loopVariables.add(rpListePrenoms);
        loopVariables.add(rpPrenomConj);
        loopVariables.add(rpAnaisConj);
        loopVariables.add(rpSexConj);
        loopVariables.addAll(loopVariableMapFirstNameParent.values());
        loopVariables.addAll(loopVariableMapBirthYearParent.values());
        loopVariables.addAll(loopVariableMapGenderParent.values());
        loopVariables.addAll(loopVariableMapFirstNameChildren.values());
        loopVariables.addAll(loopVariableMapBirthYearChildren.values());

        return ExternalsVariables.builder()
                .simpleVariables(simpleVariables)
                .loopVariables(loopVariables)
                .build();

    }
}
