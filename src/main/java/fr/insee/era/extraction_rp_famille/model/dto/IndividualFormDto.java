package fr.insee.era.extraction_rp_famille.model.dto;

import fr.insee.era.extraction_rp_famille.model.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@Slf4j
public class IndividualFormDto {
    private long id;
    private String lastName;
    private String firstName;
    private String gender;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String dpnaicode;
    private String cnaif;
    private String cnaie;
    private String pnai;
    private boolean surveyed;
    private boolean major;
    private List<RelationshipDto> familyTies;

    public long countChildren() {
        return familyTies.stream().filter(r -> r.getRelationshipType() == RelationshipType.PARENT).count();
    }

    public LocalDate getBirthDate() {
        if (StringUtils.isBlank(birthYear) && !StringUtils.isNumeric(birthYear)) {
            return LocalDate.of(1980, 12, 31);
        }
        if (StringUtils.isBlank(birthMonth) && !StringUtils.isNumeric(birthMonth)) {
            return LocalDate.of(Integer.parseInt(birthYear), 12, 31);
        }
        if (StringUtils.isBlank(birthDay) && !StringUtils.isNumeric(birthDay)) {
            return LocalDate.of(Integer.parseInt(birthYear), Integer.parseInt(birthMonth), 31);
        }
        return LocalDate.of(Integer.parseInt(birthYear), Integer.parseInt(birthMonth), Integer.parseInt(birthDay));
    }




}
