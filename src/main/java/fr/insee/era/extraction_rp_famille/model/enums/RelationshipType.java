package fr.insee.era.extraction_rp_famille.model.enums;

import lombok.Getter;

@Getter
public enum RelationshipType {
    ENQUETE(0, "ENQ"), CONJOINT(1, "CONJ"),PARENT(2, "PAR"),ENFANT(3, "ENF");

    private int value;
    private String label;

    RelationshipType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public static RelationshipType fromValue(int value) {
        return switch (value) {
            case 0 -> ENQUETE;
            case 1 -> CONJOINT;
            case 2 -> PARENT;
            case 3 -> ENFANT;
            default -> null;
        };
    }

}
