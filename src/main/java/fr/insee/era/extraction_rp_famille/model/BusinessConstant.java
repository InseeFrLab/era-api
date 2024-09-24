package fr.insee.era.extraction_rp_famille.model;

public final class BusinessConstant {

    private BusinessConstant() {
        throw new IllegalStateException("Utility class");
    }
    public static final int MAX_SURVEYED_PERSONS = 10;
    public static final int MAX_CHILDREN_PER_PERSON = 8;
}
