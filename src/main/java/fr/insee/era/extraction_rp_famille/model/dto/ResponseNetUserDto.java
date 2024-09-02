package fr.insee.era.extraction_rp_famille.model.dto;

import fr.insee.era.extraction_rp_famille.model.BusinessConstant;
import fr.insee.era.extraction_rp_famille.model.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Builder
@Data
public class ResponseNetUserDto {

    private Long id;
    private String identifiant;
    private Long idinternaute;
    private String mail;
    private String depcom;
    private String iris;
    private String numvoiloc;
    private String bisterloc;
    private String typevoiloc;
    private String nomvoiloc;
    private String resloc;
    private String cpostloc;
    private String car;
    private List<IndividualFormDto> individuals;

    public boolean isValid() {
        return StringUtils.isNotBlank(mail);
    }

    public long countSurveyedAndMajor() {
        long count = individuals.stream().filter(i -> i.isSurveyed() && i.isMajor()).count();
        return Math.min(count, BusinessConstant.MAX_SURVEYED_PERSONS);
    }

    public long maxNumberOfChildren() {
        long maxChildren = individuals.stream().filter(i -> i.isSurveyed() && i.isMajor())
                .mapToLong(IndividualFormDto::countChildren).max().orElse(0);
        return Math.min(maxChildren, BusinessConstant.MAX_CHILDREN_PER_PERSON);
    }

    public List<IndividualFormDto> getListOfSurveyedMajor() {
        return individuals.stream().filter(i -> i.isSurveyed() && i.isMajor())
                .sorted(Comparator.comparing(IndividualFormDto::getBirthDate))
                .limit(BusinessConstant.MAX_SURVEYED_PERSONS).toList();
    }

    public Map<Integer, IndividualFormDto> getListOfParentsByIndividual(IndividualFormDto individual) {
        List<Long> parentsIds = individual.getFamilyTies().stream()
                .filter(r -> r.getRelationshipType() == RelationshipType.ENFANT)
                .map(RelationshipDto::getIdRelationship).toList();
        List<IndividualFormDto> parentsList = individuals.stream()
                .filter(i -> parentsIds.contains(i.getId()))
                .sorted(Comparator.comparing(IndividualFormDto::getBirthDate))
                .limit(2).toList();

        Map<Integer, IndividualFormDto> parentsMap = new HashMap<>();
        parentsMap.put(1, IndividualFormDto.builder().build());
        parentsMap.put(2, IndividualFormDto.builder().build());

        for (int i = 1; i <= parentsList.size(); i++) {
            parentsMap.put(i, parentsList.get(i - 1));
        }
        return parentsMap;
    }

    public IndividualFormDto getConjointByIndividual(IndividualFormDto individual) {
        Optional<Long> conjointId = individual.getFamilyTies().stream()
                .filter(r -> r.getRelationshipType() == RelationshipType.CONJOINT)
                .map(RelationshipDto::getIdRelationship).findFirst();
        if (conjointId.isEmpty()) {
            return IndividualFormDto.builder().build();
        }
        return individuals.stream().filter(i -> i.getId() == conjointId.get()).findFirst().orElse(IndividualFormDto.builder().build());
    }

}
