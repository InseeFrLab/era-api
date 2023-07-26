package fr.insee.era.extraction_rp_famille.model.enums;

import lombok.Getter;

@Getter
public enum GenderType {
    MALE("1", "hommes"), FEMALE("2", "femmes");

    private final String value;
    private final String label;

    GenderType(String value, String label) {
        this.value = value;
        this.label = label;
    }

}
