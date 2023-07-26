package fr.insee.era.extraction_rp_famille.model.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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

}
