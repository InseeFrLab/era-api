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

    private static final String EMPTY_STRING = "";

    /**
     * Parse a response to generate a CSV line
     *
     * @param idCampaign         : one of the columns of the CSV is this value
     * @param responseNetUserDto : The reponse to parse
     * @param gender
     * @param lineSize           : number of columns
     * @return a String array representing the columns of a CSV line
     */
    public String[] convert(String idCampaign, ResponseNetUserDto responseNetUserDto, GenderType gender, int lineSize) {
        List<String> line = new ArrayList<>(lineSize);

        List<IndividualFormDto> listOfSurveyedMajor = responseNetUserDto.getListOfSurveyedMajor();

        line.add(String.valueOf(responseNetUserDto.getId()));
        line.add(idCampaign);
        line.add(responseNetUserDto.getIdentifiant());
        line.add(gender.getValue()); //Lot == gender
        line.add(EMPTY_STRING); //civilite
        line.add(EMPTY_STRING);//Nom
        line.add(EMPTY_STRING); //Prenom
        line.add(responseNetUserDto.getMail());
        line.add(responseNetUserDto.getNumvoiloc());
        line.add(responseNetUserDto.getBisterloc());
        line.add(responseNetUserDto.getTypevoiloc());
        line.add(responseNetUserDto.getNomvoiloc());
        line.add(responseNetUserDto.getResloc());
        line.add(EMPTY_STRING); //Mention spéciale
        line.add(responseNetUserDto.getCpostloc());
        line.add(responseNetUserDto.getCar());
        line.add(EMPTY_STRING);//NomUe
        line.add(EMPTY_STRING);//PrenomUe
        line.add(EMPTY_STRING);//AnneeNaissanceUe
        line.add(gender.getValue());//TYPE_QUEST
        line.add(gender.getLabel());//RPTYPEQUEST
        line.add(String.valueOf(responseNetUserDto.countSurveyedAndMajor())); //RPNBQUEST

        String whoAnswers = gender == GenderType.MALE ? "homme âgé" : "femme âgée";
        line.add(String.format("Dans votre foyer, chaque %s de 18 ans ou plus doit répondre : ", whoAnswers)); //whoAnswers1

        String firstNameList = listOfSurveyedMajor.stream().map(IndividualFormDto::getFirstName).collect(Collectors.joining(", "));
        line.add(firstNameList);//whoAnswers2

        line.add(EMPTY_STRING); //whoAnswers3
        line.add(firstNameList);//RPLISTEPRENOMS

        // add external line
        //TODO : on est sur max surveyd_persons ou int maxPerson = censusRespondents.stream().map(ResponseNetUserDto::countSurveyedAndMajor).max(Long::compareTo).orElse(0L).intValue();
        for (int i = 0; i < BusinessConstant.MAX_SURVEYED_PERSONS; i++) {

            //Une fois qu'on a traité tous les majeurs surveyed, on mets les valeurs par défaut d'un individus null
            if (i >= listOfSurveyedMajor.size()) {
                //Surveyed
                line.add(EMPTY_STRING); //firstname
                line.add(EMPTY_STRING); //birth year

                //Conjoint
                getExternalLine(null).forEach(line::add);

                //parents
                getExternalLine(null).forEach(line::add);
                getExternalLine(null).forEach(line::add);

                continue;
            }

            IndividualFormDto individual = listOfSurveyedMajor.get(i);

            //Surveyed
            getExternalLine(individual).forEach(line::add);

            //Conjoint
            IndividualFormDto conjoint = responseNetUserDto.getConjointByIndividual(individual);
            getExternalLine(conjoint).forEach(line::add);

            //parents
            Map<Integer, IndividualFormDto> parents = responseNetUserDto.getListOfParentsByIndividual(individual);
            getExternalLine(parents.get(1)).forEach(line::add);
            getExternalLine(parents.get(2)).forEach(line::add);
        }

        return line.toArray(new String[0]);
    }

    private List<String> getExternalLine(IndividualFormDto individual) {

        List<String> externalLine = new ArrayList<>();
        if (individual == null) {
            individual = IndividualFormDto.builder().build();
        }
        String id = individual.getId() == 0 ? EMPTY_STRING : String.valueOf(individual.getId());
        externalLine.add(id);
        externalLine.add(individual.getFirstName());
        externalLine.add(individual.getLastName());
        externalLine.add(individual.getBirthYear());
        externalLine.add(StringUtils.leftPad(individual.getBirthMonth(),2,'0'));
        externalLine.add(StringUtils.leftPad(individual.getBirthDay(),2,'0'));
        externalLine.add(individual.getGender());
        externalLine.add(individual.getDpnaicode());
        externalLine.add(individual.getCnaif());
        externalLine.add(individual.getCnaie());
        externalLine.add(individual.getPnai());

        externalLine.add(individual.getNaiP1());//RPNAIPAR1
        externalLine.add(individual.getPaysNaiP1());//RPPNAIPAR1
        externalLine.add(individual.getNaiP2());//RPNAIPAR2
        externalLine.add(individual.getPaysNaiP2());//RPPNAIPAR2

        return externalLine;
    }


    public String[] writeHeader(int maxSurveydPersons, int maxChildrenPerPerson) {

        List<String> commonHeader = new ArrayList<>(Arrays.asList("IdentifiantExterne", "IdModele", "IdeC", "IdLot", "Civilite", "Nom", "Prenom", "AdresseMessagerie", "NumeroVoie", "IndiceRepetition", "TypeVoie", "LibelleVoie", "ComplementAdresse", "MentionSpeciale", "CodePostal", "LibelleCommune", "NomUe", "PrenomUe", "AnneeNaissanceUe", "TYPE_QUEST", "RPTYPEQUEST", "RPNBQUEST", "whoAnswers1", "whoAnswers2", "whoAnswers3", "RPLISTEPRENOMS"));

        List<String> externalsHeader = new ArrayList<>();
        //add externals headers
        for (int i = 0; i < maxSurveydPersons; i++) {
            //Surveyed
            externalsHeader.addAll(getExternalsHeaderByType("ENQ", i));

            //Conjoint
            externalsHeader.addAll(getExternalsHeaderByType("CONJ", i));

            //parent1
            externalsHeader.addAll(getExternalsHeaderByType("PAR1", i));

            //parent2
            externalsHeader.addAll(getExternalsHeaderByType("PAR2", i));

            //Enfants
            for (int j = 1; j <= maxChildrenPerPerson; j++) {
                externalsHeader.addAll(getExternalsHeaderByType("ENF" + j, i));
            }
        }

        return Stream.of(commonHeader, externalsHeader).flatMap(Collection::stream).toArray(String[]::new);
    }

    private List<String> getExternalsHeaderByType(String type, int index) {
        List<String> externalsHeader = new ArrayList<>();
        externalsHeader.add(String.format("RPID%s_%d", type, index));
        externalsHeader.add(String.format("RPPRENOM%s_%d", type, index));
        externalsHeader.add(String.format("RPNOM%s_%d", type, index));
        externalsHeader.add(String.format("RPANAIS%s_%d", type, index));
        externalsHeader.add(String.format("RPMNAIS%s_%d", type, index));
        externalsHeader.add(String.format("RPJNAIS%s_%d", type, index));
        externalsHeader.add(String.format("RPSEX%s_%d", type, index));
        externalsHeader.add(String.format("RPDPNAICODE%s_%d", type, index));
        externalsHeader.add(String.format("RPCNAIF%s_%d", type, index));
        externalsHeader.add(String.format("RPCNAIE%s_%d", type, index));
        externalsHeader.add(String.format("RPPNAI%s_%d", type, index));
        externalsHeader.add(String.format("RPNAIPAR1%s_%d", type, index));
        externalsHeader.add(String.format("RPPNAIPAR1%s_%d", type, index));
        externalsHeader.add(String.format("RPNAIPAR2%s_%d", type, index));
        externalsHeader.add(String.format("RPPNAIPAR2%s_%d", type, index));
        return externalsHeader;
    }

}
