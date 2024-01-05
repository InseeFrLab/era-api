package fr.insee.era.extraction_rp_famille.service.impl;

import fr.insee.era.extraction_rp_famille.model.dto.IndividualFormDto;
import fr.insee.era.extraction_rp_famille.model.dto.ResponseNetUserDto;
import fr.insee.era.extraction_rp_famille.model.enums.GenderType;
import fr.insee.era.extraction_rp_famille.service.BusinessRulesService;
import fr.insee.era.extraction_rp_famille.utils.CogBean;
import fr.insee.era.extraction_rp_famille.utils.CsvToBeanUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BusinessRulesServiceImpl implements BusinessRulesService {
    @Override
    public List<IndividualFormDto> filterIndividual(List<IndividualFormDto> individuals, GenderType gender) {
        if (individuals == null || individuals.isEmpty()) {
            return individuals;
        }
        // flag major
        individuals.forEach(i -> i.setMajor(isMajor(i.getBirthDate())));
        // flag surveyed
        individuals.forEach(i -> i.setSurveyed(gender.getValue().equals(i.getGender())));
        // remove if no major surveyed
        individuals = removeIfNoMajorSurveyed(individuals);
        // sort
        individuals = individuals.stream().sorted((i1, i2) -> {
            LocalDate birthDate1 = i1.getBirthDate();
            LocalDate birthDate2 = i2.getBirthDate();
            return birthDate1.compareTo(birthDate2);
        }).toList();

        return individuals;
    }

    @Override
    public List<ResponseNetUserDto> filterResponseNetUser(List<ResponseNetUserDto> responses) {
        if (responses == null || responses.isEmpty()) {
            return responses;
        }
        // remove if not valid and if no individuals
        return responses.stream().filter(r -> r.isValid() && !r.getIndividuals().isEmpty()).toList();
    }

    @Override
    public List<ResponseNetUserDto> completeResponseNetUser(List<ResponseNetUserDto> responses) {
        if (responses == null || responses.isEmpty()) {
            return responses;
        }
        List<CogBean> cogBeans = CsvToBeanUtils.parse("correspondance_cog_cod_post.csv", CogBean.class);
        Map<String, String> cogMap = cogBeans.stream().collect(Collectors.toMap(CogBean::getDepCom, CogBean::getZipCode));
        for (ResponseNetUserDto response : responses) {
            String zipCode = cogMap.get(response.getDepcom());
            if (zipCode != null) {
                response.setCpostloc(zipCode);
            }
        }
        return responses;
    }

    private List<IndividualFormDto> removeIfNoMajorSurveyed(List<IndividualFormDto> individuals) {
        long majorSurveyedCount = individuals.stream().filter(i -> i.isMajor() && i.isSurveyed()).count();
        if (majorSurveyedCount == 0) {
            return List.of();
        }
        return individuals;
    }

    private boolean isMajor(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        LocalDate majorityDate = birthDate.plusYears(18);
        LocalDate startYear = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        return !majorityDate.isAfter(startYear); // majorityDate <= startYear
    }

}
