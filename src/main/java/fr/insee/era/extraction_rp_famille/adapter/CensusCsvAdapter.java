package fr.insee.era.extraction_rp_famille.adapter;

import fr.insee.era.extraction_rp_famille.model.BusinessConstant;
import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CensusCsvAdapter {

    public String[] convert(String idCampaign, ResponseNetUserDto responseNetUserDto, GenderType gender, int lineSize) {
        String[] line = new String[lineSize];

        // add common line
        Map<Integer, String> commonLine = getCommonLine(idCampaign, responseNetUserDto, gender);
        commonLine.forEach((key, value) -> line[key] = value);
        // add external line
        List<String> externalLine = new ArrayList<>();
        List<IndividualFormDto> listOfSurveyedMajor = responseNetUserDto.getListOfSurveyedMajor();
        String firstNameList = listOfSurveyedMajor.stream()
                .map(IndividualFormDto::getFirstName).collect(Collectors.joining(", "));
        //rplistePrenoms
        for (int i = 0; i < BusinessConstant.MAX_SURVEYED_PERSONS; i++) {
            if (i >= listOfSurveyedMajor.size()) {
                externalLine.add("");
                continue;
            }
            externalLine.add(firstNameList);
        }

        for (int i = 0; i < BusinessConstant.MAX_SURVEYED_PERSONS; i++) {

            if (i >= listOfSurveyedMajor.size()) {
                //Conjoint
                externalLine.addAll(getExternalLine(null, true, false));

                //parents
                externalLine.addAll(getExternalLine(null, true, false));
                externalLine.addAll(getExternalLine(null, true, false));

                // Children
                for (int j = 1; j <= BusinessConstant.MAX_CHILDREN_PER_PERSON; j++) {
                    externalLine.addAll(getExternalLine(null, false, false));
                }
                continue;
            }

            IndividualFormDto individual = listOfSurveyedMajor.get(i);

            //Conjoint
            IndividualFormDto conjoint = responseNetUserDto.getConjointByIndividual(individual);
            externalLine.addAll(getExternalLine(conjoint, true, false));

            //parents

            Map<Integer,IndividualFormDto> parents = responseNetUserDto.getListOfParentsByIndividual(individual);
            externalLine.addAll(getExternalLine(parents.get(1), true, false));
            externalLine.addAll(getExternalLine(parents.get(2), true, false));

            // Children
            Map<Integer,IndividualFormDto> children = responseNetUserDto.getListOfChildrenByIndividual(individual);
            for (int j = 1; j <= BusinessConstant.MAX_CHILDREN_PER_PERSON; j++) {
                externalLine.addAll(getExternalLine(children.get(j), false, false));
            }
        }
        for (int i = 25; i < lineSize; i++) {
            line[i] = externalLine.get(i - 25);
        }
        return line;
    }

    private List<String> getExternalLine(IndividualFormDto individual, boolean withGender, boolean withSupplements) {

        List<String> externalLine = new ArrayList<>();
        if (individual == null) {
            individual = IndividualFormDto.builder().build();
        }
        String id = individual.getId() == 0 ? "" : String.valueOf(individual.getId());
        externalLine = addToListWithCondition(externalLine, id, withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getFirstName(), true);
        externalLine = addToListWithCondition(externalLine, individual.getLastName(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getBirthYear(), true);
        externalLine = addToListWithCondition(externalLine, individual.getBirthMonth(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getBirthDay(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getGender(), withGender);
        externalLine = addToListWithCondition(externalLine, individual.getDpnaicode(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getCnaif(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getCnaie(), withSupplements);
        externalLine = addToListWithCondition(externalLine, individual.getPnai(), withSupplements);
        return externalLine;
    }


    private Map<Integer, String> getCommonLine(String idCampaign, ResponseNetUserDto responseNetUserDto,
                                               GenderType gender) {
        Map<Integer, String> commonLine = new HashMap<>();
        commonLine.put(0, String.valueOf(responseNetUserDto.getId()));
        commonLine.put(1, idCampaign);
        commonLine.put(2, responseNetUserDto.getIdentifiant());
        commonLine.put(3, gender.getValue());
        commonLine.put(7, responseNetUserDto.getMail());
        commonLine.put(8, responseNetUserDto.getNumvoiloc());
        commonLine.put(9, responseNetUserDto.getBisterloc());
        commonLine.put(10, responseNetUserDto.getTypevoiloc());
        commonLine.put(11, responseNetUserDto.getNomvoiloc());
        commonLine.put(12, responseNetUserDto.getResloc());
        commonLine.put(14, responseNetUserDto.getCpostloc());
        commonLine.put(15, responseNetUserDto.getCar());
        commonLine.put(19, gender.getValue());
        commonLine.put(20, gender.getLabel());
        commonLine.put(21, String.valueOf(responseNetUserDto.countSurveyedAndMajor()));
        String whoAnswers = gender == GenderType.MALE ? "homme âgé" : "femme âgée";
        commonLine.put(22, String.format("Dans votre foyer, chaque %s de 18 ans ou plus doit répondre : ",
                whoAnswers));
        List<IndividualFormDto> listOfSurveyedMajor = responseNetUserDto.getListOfSurveyedMajor();
        String firstNameList = listOfSurveyedMajor.stream()
                .map(IndividualFormDto::getFirstName).collect(Collectors.joining(", "));
        commonLine.put(23, firstNameList);
        return commonLine;
    }

    public String[] writeHeader() {

        List<String> commonHeader = new ArrayList<>(Arrays.asList("Identifiant", "IdModele", "IdeC", "IdLot",
                "CiviliteReferent", "NomReferent", "PrenomReferent", "MailReferent", "NumeroVoie", "IndiceRepetition"
                , "TypeVoie", "LibelleVoie", "ComplementAdresse", "MentionSpeciale", "CodePostal", "LibelleCommune",
                "NomUe", "PrenomUe", "AnneeNaissanceUe", "TYPE_QUEST", "RPTYPEQUEST", "RPNBQUEST", "whoAnswers1",
                "whoAnswers2", "whoAnswers3"));

        List<String> externalsHeader = new ArrayList<>();
        //add externals headers
        //rplistePrenoms
        for (int i = 0; i < BusinessConstant.MAX_SURVEYED_PERSONS; i++) {
            externalsHeader.add(String.format("RPLISTEPRENOMS_%d", i));
        }
        for (int i = 0; i < BusinessConstant.MAX_SURVEYED_PERSONS; i++) {
            //Conjoint
            externalsHeader.addAll(getExternalsHeaderByType("CONJ", i, true, false));

            //parent1
            externalsHeader.addAll(getExternalsHeaderByType("PAR1", i, true, false));

            //parent2
            externalsHeader.addAll(getExternalsHeaderByType("PAR2", i, true, false));

            //Enfants
            for (int j = 1; j <= BusinessConstant.MAX_CHILDREN_PER_PERSON; j++) {
                externalsHeader.addAll(getExternalsHeaderByType("ENF" + j, i, false, false));
            }
        }

        return Stream.of(commonHeader, externalsHeader).flatMap(Collection::stream).toArray(String[]::new);
    }

    private List<String> getExternalsHeaderByType(String type, int index, boolean withGender, boolean withSupplements) {
        List<String> externalsHeader = new ArrayList<>();
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPID%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPPRENOM%s_%d", type, index), true);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPNOM%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPANAIS%s_%d", type, index), true);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPMNAIS%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPJNAIS%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPSEX%s_%d", type, index), withGender);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPDPNAICODE%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPCNAIF%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPCNAIE%s_%d", type, index), withSupplements);
        externalsHeader = addToListWithCondition(externalsHeader, String.format("RPPNAI%s_%d", type, index), withSupplements);
        return externalsHeader;
    }

    private List<String> addToListWithCondition(List<String> list, String value, boolean condition) {
        if (StringUtils.isBlank(value)) {
            value = "";
        }
        if (condition) {
            list.add(value);
        }
        return list;
    }



    public String[] convertSupplements(ResponseNetUserDto responseNetUserDto, int lineSize,
                                       int maxPerson, int maxChildren) {
        String[] line = new String[lineSize];

        // add id rim
        line[0] =String.valueOf(responseNetUserDto.getId());

        // add supplements
        List<String> supplementsLine = new ArrayList<>();
        List<IndividualFormDto> listOfSurveyedMajor = responseNetUserDto.getListOfSurveyedMajor();

        for (int i = 0; i < maxPerson; i++) {
            if (i >= listOfSurveyedMajor.size()) {
                //Surveyed
                supplementsLine.addAll(getExternalLine(null, true, true));

                //Conjoint
                supplementsLine.addAll(getExternalLine(null, true, true));

                //parents
                supplementsLine.addAll(getExternalLine(null, true, true));
                supplementsLine.addAll(getExternalLine(null, true, true));

                // Children
                for (int j = 1; j <= maxChildren; j++) {
                    supplementsLine.addAll(getExternalLine(null, true, true));
                }
                continue;
            }
            IndividualFormDto individual = listOfSurveyedMajor.get(i);

            //Surveyed
            supplementsLine.addAll(getExternalLine(individual, true, true));

            //Conjoint
            IndividualFormDto conjoint = responseNetUserDto.getConjointByIndividual(individual);
            supplementsLine.addAll(getExternalLine(conjoint, true, true));

            //parents

            Map<Integer,IndividualFormDto> parents = responseNetUserDto.getListOfParentsByIndividual(individual);
            supplementsLine.addAll(getExternalLine(parents.get(1), true, true));
            supplementsLine.addAll(getExternalLine(parents.get(2), true, true));

            // Children
            Map<Integer,IndividualFormDto> children = responseNetUserDto.getListOfChildrenByIndividual(individual);
            for (int j = 1; j <= maxChildren; j++) {
                supplementsLine.addAll(getExternalLine(children.get(j), true, true));
            }
        }
        for (int i = 1; i < lineSize; i++) {
            line[i] = supplementsLine.get(i-1);
        }
        return line;
    }

    public String[] writeHeaderSupplements(int maxPerson, int maxChildren) {

        List<String> headers = new ArrayList<>();
        headers.add("Identifiant");
        for (int i = 0; i < maxPerson; i++) {
            //Surveyed
            headers.addAll(getExternalsHeaderByType("ENQ", i, true, true));

            //Conjoint
            headers.addAll(getExternalsHeaderByType("CONJ", i, true, true));

            //parent1
            headers.addAll(getExternalsHeaderByType("PAR1", i, true, true));

            //parent2
            headers.addAll(getExternalsHeaderByType("PAR2", i, true, true));

            //Enfants
            for (int j = 1; j <= maxChildren; j++) {
                headers.addAll(getExternalsHeaderByType("ENF" + j, i, true, true));
            }
        }

        return headers.toArray(String[]::new);
    }
}
