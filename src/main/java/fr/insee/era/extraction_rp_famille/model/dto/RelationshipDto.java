package fr.insee.era.extraction_rp_famille.model.dto;

import fr.insee.era.extraction_rp_famille.model.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RelationshipDto {
    private RelationshipType relationshipType;
    private long idRelationship;
}
